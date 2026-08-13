package test.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static test.SimpleBaseTest.getPathToResource;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.Yaml;
import org.testng.xml.SuiteDigest;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlDefine;
import org.testng.xml.XmlDependencies;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlScript;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Characterization tests over every YAML file of the test corpus, pinning the YAML reader ({@link
 * Yaml#parse}) and the YAML writer ({@link Yaml#toYaml}) as a pair -- the counterpart of {@code
 * XmlRoundTripTest} for the other suite format.
 *
 * <p>Four invariants are checked over the YAML corpus, because none of them is sufficient on its
 * own: the output must load under a plain YAML parser, which is the property the writer used to
 * violate outright; it must be a fixed point, which pins key selection and layout; the parsed model
 * must survive unchanged, which pins the data (see {@link SuiteDigest}); and it must contain no
 * anchor, since an accidentally shared collection produces an alias that loads perfectly well and
 * would slip past the other three.
 *
 * <p>A fifth one runs over the XML corpus, since that is what the {@code Converter} CLI converts
 * and it reaches constructs no YAML fixture can declare.
 */
public class YamlRoundTripTest {

  /**
   * The predicate of GITHUB-3318, stated so that it does not depend on TestNG's own binding: what
   * {@code toYaml} writes must be readable by any YAML parser.
   *
   * <p>Duplicate keys are rejected rather than tolerated, because a writer that emits the same
   * mapping key several times -- {@code packages:} used to come out three times -- produces a
   * document that snakeyaml accepts by default, silently keeping the last occurrence.
   */
  @Test(dataProvider = "yamlSuites")
  public void emittedYamlLoadsUnderAPlainYamlParser(String suiteFile) throws IOException {
    String emitted = Yaml.toYaml(parseFile(suiteFile)).toString();

    LoaderOptions options = new LoaderOptions();
    options.setAllowDuplicateKeys(false);
    org.yaml.snakeyaml.Yaml plainYaml = new org.yaml.snakeyaml.Yaml(new SafeConstructor(options));

    assertThat(plainYaml.<Object>load(emitted))
        .as("the YAML written for %s must load under a plain YAML parser:%n%s", suiteFile, emitted)
        .isInstanceOf(java.util.Map.class);
  }

  @Test(dataProvider = "yamlSuites")
  public void emittedYamlIsAFixedPoint(String suiteFile) throws IOException {
    String firstPass = Yaml.toYaml(parseFile(suiteFile)).toString();
    String secondPass = Yaml.toYaml(parseString(suiteFile, firstPass)).toString();

    assertThat(secondPass)
        .as("re-writing the suite parsed back from %s must be a fixed point", suiteFile)
        .isEqualTo(firstPass);
  }

  @Test(dataProvider = "yamlSuites")
  public void suiteContentSurvivesTheRoundTrip(String suiteFile) throws IOException {
    XmlSuite parsedFromFile = parseFile(suiteFile);
    XmlSuite reparsed = parseString(suiteFile, Yaml.toYaml(parsedFromFile).toString());

    assertThat(SuiteDigest.of(reparsed))
        .as(
            "the suite parsed back from the YAML written for %s must carry the same data",
            suiteFile)
        .isEqualTo(SuiteDigest.of(parsedFromFile));
  }

  /**
   * Putting the same collection instance in two places of the document makes snakeyaml emit an
   * anchor and an alias. That still loads, and it still round trips, so only an assertion on the
   * text catches it -- and a suite file full of {@code *id001} is not something to hand to a user.
   */
  @Test(dataProvider = "yamlSuites")
  public void emittedYamlUsesNoAnchors(String suiteFile) throws IOException {
    String emitted = Yaml.toYaml(parseFile(suiteFile)).toString();

    assertThat(emitted)
        .as("the YAML written for %s must not reference shared nodes through aliases", suiteFile)
        .doesNotContainPattern("&id\\d+");
  }

  /**
   * The other direction, which is what the {@code Converter} CLI does: an XML suite must convert to
   * YAML the reader accepts.
   *
   * <p>Only loadability is asserted, not the round trip. XML expresses more than the YAML schema
   * does -- a suite level {@code <define>} has no key, and the reader numbers includes from zero
   * whereas the XML parser numbers them across the whole class -- so comparing digests would fail
   * for reasons that have nothing to do with the writer. Loadability alone is enough to catch a key
   * being written that nothing can read back, which the YAML corpus cannot: it can only contain
   * what YAML can already express.
   *
   * <p>The input is {@link #everySuiteConstruct()} rather than a corpus of real files. A corpus
   * samples what people happen to write and covers the writer's branches by accident; the suite
   * built there sets every construct the writer looks at, on purpose.
   */
  @Test(dataProvider = "xmlSuites")
  public void xmlSuitesConvertToLoadableYaml(String name, XmlSuite xmlSuite) {
    String emitted = Yaml.toYaml(xmlSuite).toString();

    assertThatCode(() -> parseString(name, emitted))
        .as("the YAML written for %s must be readable back:%n%s", name, emitted)
        .doesNotThrowAnyException();
  }

  /** The XML suites of this module, plus one that carries every construct at once. */
  @DataProvider(name = "xmlSuites")
  public static Object[][] xmlSuites() throws IOException {
    Path root = Paths.get(getPathToResource(""));
    try (Stream<Path> paths = Files.walk(root)) {
      List<Object[]> suites =
          paths
              .filter(Files::isRegularFile)
              .filter(path -> path.getFileName().toString().endsWith(".xml"))
              .sorted()
              .map(
                  path -> {
                    String name = root.relativize(path).toString();
                    return new Object[] {name, parseXml(name, path)};
                  })
              .collect(Collectors.toCollection(ArrayList::new));
      suites.add(new Object[] {"every suite construct", everySuiteConstruct()});
      return suites.toArray(new Object[0][]);
    }
  }

  private static XmlSuite parseXml(String name, Path path) {
    try (InputStream stream = Files.newInputStream(path)) {
      return new SuiteXmlParser().parse(name, stream, false);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * A suite that sets everything a suite file can carry, including what no fixture declares: the
   * constructs the YAML schema has no key for. Writing one of those would produce a document the
   * reader rejects, which is exactly what this is here to catch.
   */
  private static XmlSuite everySuiteConstruct() {
    XmlSuite suite = new XmlSuite();
    suite.setName("Every construct");
    suite.setVerbose(3);
    suite.setParallel(XmlSuite.ParallelMode.METHODS);
    suite.setThreadCount(7);
    suite.setDataProviderThreadCount(5);
    suite.setTimeOut("4000");
    suite.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
    suite.setSkipFailedInvocationCounts(true);
    suite.setPreserveOrder(false);
    suite.setGroupByInstances(true);
    suite.setAllowReturnValues(true);
    suite.setShareThreadPoolForDataProviders(true);
    suite.shouldUseGlobalThreadPool(true);
    suite.setParentModule("com.example.Module");
    suite.setGuiceStage("PRODUCTION");
    suite.setListeners(new ArrayList<>(Collections.singletonList("com.example.Listener")));
    suite.setSuiteFiles(new ArrayList<>(Collections.singletonList("child.xml")));
    suite.setParameters(parameters("suite"));
    suite.setIncludedGroups(Collections.singletonList("in"));
    suite.setExcludedGroups(Collections.singletonList("out"));
    // A suite level <define> and <dependencies>: neither has a YAML key, so both must be dropped.
    XmlDefine define = new XmlDefine();
    define.setName("meta");
    define.getIncludes().add("in");
    suite.getGroups().addDefine(define);
    XmlDependencies dependencies = new XmlDependencies();
    dependencies.onGroup("in", "out");
    suite.getGroups().setXmlDependencies(dependencies);
    suite.getXmlPackages().add(filteredPackage("com.example.suite"));
    suite.getMethodSelectors().add(selectorClass());
    suite.getMethodSelectors().add(selectorScript());

    XmlTest test = new XmlTest(suite);
    test.setName("Every construct");
    test.setVerbose(4);
    test.setParallel(XmlSuite.ParallelMode.CLASSES);
    test.setThreadCount(9);
    test.setTimeOut(1234);
    test.setPreserveOrder(true);
    test.setGroupByInstances(false);
    test.setAllowReturnValues(false);
    test.setSkipFailedInvocationCounts(false);
    test.setParameters(parameters("test"));
    test.setIncludedGroups(Collections.singletonList("ti"));
    test.setExcludedGroups(Collections.singletonList("te"));
    test.addMetaGroup("all", "ti", "te");
    test.setXmlDependencyGroups(Collections.singletonMap("ti", "te"));
    test.getMethodSelectors().add(selectorScript());
    test.getXmlPackages().add(filteredPackage("com.example.test"));
    test.getXmlClasses().add(everyClassConstruct());
    return suite;
  }

  private static XmlClass everyClassConstruct() {
    XmlClass xmlClass = new XmlClass("com.example.Klass", 0, false);
    xmlClass.setParameters(parameters("class"));
    xmlClass.getExcludedMethods().add("excluded");
    XmlInclude include = new XmlInclude("included", Collections.singletonList(2), 0);
    include.setDescription("what it does");
    include.setParameters(parameters("include"));
    xmlClass.getIncludedMethods().add(include);
    return xmlClass;
  }

  private static XmlPackage filteredPackage(String name) {
    XmlPackage xmlPackage = new XmlPackage(name);
    xmlPackage.getInclude().add("Foo");
    xmlPackage.getExclude().add("Bar");
    return xmlPackage;
  }

  private static XmlMethodSelector selectorClass() {
    XmlMethodSelector selector = new XmlMethodSelector();
    selector.setClassName("com.example.Selector");
    selector.setPriority(3);
    return selector;
  }

  private static XmlMethodSelector selectorScript() {
    XmlMethodSelector selector = new XmlMethodSelector();
    XmlScript script = new XmlScript();
    script.setExpression("groups.containsKey(\"x\")");
    script.setLanguage("beanshell");
    selector.setScript(script);
    return selector;
  }

  /** Values that need quoting on the way out, one per trap the emitter has to handle. */
  private static Map<String, String> parameters(String level) {
    Map<String, String> parameters = new LinkedHashMap<>();
    parameters.put(level, "plain");
    parameters.put(level + ".comma", "a,b");
    parameters.put(level + ".spaces", "a  b");
    parameters.put(level + ".number", "44.0");
    parameters.put(level + ".boolean", "off");
    return parameters;
  }

  /**
   * Every YAML file of the test corpus.
   *
   * <p>The filter is the extension alone, because that is exactly what {@code YamlParser.accept}
   * promises: a {@code .yaml} or {@code .yml} file under the resources root is a suite file. Adding
   * one therefore extends the corpus without touching this class.
   */
  @DataProvider(name = "yamlSuites")
  public static Object[][] yamlSuites() throws IOException {
    Path root = Paths.get(getPathToResource(""));
    try (Stream<Path> paths = Files.walk(root)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(YamlRoundTripTest::isYaml)
          .sorted()
          .map(path -> new Object[] {root.relativize(path).toString()})
          .toArray(Object[][]::new);
    }
  }

  private static boolean isYaml(Path path) {
    String name = path.getFileName().toString();
    return name.endsWith(".yaml") || name.endsWith(".yml");
  }

  private static XmlSuite parseFile(String suiteFile) throws IOException {
    Path path = Paths.get(getPathToResource(suiteFile));
    try (InputStream stream = Files.newInputStream(path)) {
      // Classes are not loaded, so that fixtures naming a class that does not exist -- which is
      // what yaml/suiteWithNonExistentTest.yaml is for -- are part of the corpus like any other.
      return Yaml.parse(suiteFile, stream, false);
    }
  }

  private static XmlSuite parseString(String suiteFile, String yaml) throws FileNotFoundException {
    byte[] bytes = yaml.getBytes(StandardCharsets.UTF_8);
    return Yaml.parse(suiteFile, new ByteArrayInputStream(bytes), false);
  }
}
