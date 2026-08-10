package test.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static test.SimpleBaseTest.getPathToResource;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.Yaml;
import org.testng.xml.SuiteDigest;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlRoundTripTest;
import org.testng.xml.XmlSuite;
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
   */
  @Test(dataProvider = "suiteFiles", dataProviderClass = XmlRoundTripTest.class)
  public void xmlSuitesConvertToLoadableYaml(String suiteFile) throws IOException {
    Path path = Paths.get(getPathToResource(suiteFile));
    XmlSuite xmlSuite;
    try (InputStream stream = Files.newInputStream(path)) {
      xmlSuite = new SuiteXmlParser().parse(suiteFile, stream, false);
    }
    String emitted = Yaml.toYaml(xmlSuite).toString();

    assertThatCode(() -> parseString(suiteFile, emitted))
        .as("the YAML written for %s must be readable back:%n%s", suiteFile, emitted)
        .doesNotThrowAnyException();
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
