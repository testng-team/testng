package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.testng.TestNGException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * The YAML schema, stated a second time.
 *
 * <p>{@link YamlSchema} is what stops the accepted key set from being whatever {@link XmlSuite} and
 * friends happen to expose through public accessors. That guarantee is only worth something if
 * widening the set is visible, so the key names are written out here as literals rather than read
 * back from the production table: adding a key without meaning to now fails a test instead of
 * quietly extending the file format.
 *
 * <p>Documents are inline rather than fixtures, because {@code YamlRoundTripTest} sweeps every
 * {@code .yaml} file under the test resources and would pick up a file written to be rejected.
 */
public class YamlSchemaTest {

  @Test
  public void aSuiteAcceptsExactlyTheDeclaredKeys() {
    assertThat(YamlSchema.suite().acceptedKeys())
        .containsExactlyInAnyOrder(
            "name",
            "verbose",
            "parallel",
            "threadCount",
            "dataProviderThreadCount",
            "timeOut",
            "configFailurePolicy",
            "skipFailedInvocationCounts",
            "preserveOrder",
            "groupByInstances",
            "allowReturnValues",
            "shareThreadPoolForDataProviders",
            "lazyFactory",
            "parentModule",
            "guiceStage",
            "parameters",
            "listeners",
            "includedGroups",
            "excludedGroups",
            "packages",
            "methodSelectors",
            "suiteFiles",
            "tests");
  }

  @Test
  public void aTestAcceptsExactlyTheDeclaredKeys() {
    assertThat(YamlSchema.test().acceptedKeys())
        .containsExactlyInAnyOrder(
            "name",
            "verbose",
            "parallel",
            "threadCount",
            "timeOut",
            "preserveOrder",
            "groupByInstances",
            "allowReturnValues",
            "skipFailedInvocationCounts",
            "parameters",
            "includedGroups",
            "excludedGroups",
            "metaGroups",
            "dependencyGroups",
            "methodSelectors",
            "packages",
            "classes");
  }

  @Test
  public void theLeafElementsAcceptExactlyTheDeclaredKeys() {
    assertThat(YamlSchema.xmlClass(false).acceptedKeys())
        .containsExactlyInAnyOrder("name", "parameters", "includedMethods", "excludedMethods");
    assertThat(YamlSchema.xmlPackage().acceptedKeys())
        .containsExactlyInAnyOrder("name", "include", "exclude");
    assertThat(YamlSchema.include().acceptedKeys())
        .containsExactlyInAnyOrder("name", "description", "parameters");
    assertThat(YamlSchema.methodSelector().acceptedKeys())
        .containsExactlyInAnyOrder("className", "priority", "expression", "language");
  }

  @Test
  public void onlyTheKeysThatUsedToBeDerivedFromAnAccessorAreDeprecated() {
    assertThat(YamlSchema.suite().aliases()).containsExactly(entry("xmlPackages", "packages"));
    assertThat(YamlSchema.test().aliases())
        .containsOnly(
            entry("xmlDependencyGroups", "dependencyGroups"),
            entry("xmlPackages", "packages"),
            entry("xmlClasses", "classes"));
    assertThat(YamlSchema.methodSelector().aliases()).containsExactly(entry("name", "className"));
    assertThat(YamlSchema.xmlClass(false).aliases()).isEmpty();
    assertThat(YamlSchema.xmlPackage().aliases()).isEmpty();
    assertThat(YamlSchema.include().aliases()).isEmpty();
  }

  /**
   * Every suite level key, read into the place the table names. A key wired to the wrong setter --
   * or to none, which snakeyaml would report as a property that is not writable -- fails here.
   */
  @Test
  public void everySuiteKeyIsRead() throws FileNotFoundException {
    XmlSuite suite =
        parse(
            "name: Schema",
            "verbose: 3",
            "parallel: methods",
            "threadCount: 7",
            "dataProviderThreadCount: 5",
            "timeOut: 4000",
            "configFailurePolicy: continue",
            "skipFailedInvocationCounts: true",
            "preserveOrder: false",
            "groupByInstances: true",
            "allowReturnValues: true",
            "shareThreadPoolForDataProviders: true",
            "lazyFactory: true",
            "parentModule: com.example.Module",
            "guiceStage: PRODUCTION",
            "parameters: { n: 42, s: text }",
            "listeners: [ com.example.Listener ]",
            "includedGroups: [ in ]",
            "excludedGroups: [ out ]",
            "packages:",
            "  - name: com.example.included",
            "    include: [ Foo ]",
            "    exclude: [ Bar ]",
            "methodSelectors:",
            "  - className: com.example.Selector",
            "    priority: 3",
            "suiteFiles: [ child.yaml ]",
            "tests:",
            "  - name: T");

    assertThat(suite.getName()).isEqualTo("Schema");
    assertThat(suite.getVerbose()).isEqualTo(3);
    assertThat(suite.getParallel()).isEqualTo(XmlSuite.ParallelMode.METHODS);
    assertThat(suite.getThreadCount()).isEqualTo(7);
    assertThat(suite.getDataProviderThreadCount()).isEqualTo(5);
    assertThat(suite.getTimeOut()).isEqualTo("4000");
    assertThat(suite.getConfigFailurePolicy()).isEqualTo(XmlSuite.FailurePolicy.CONTINUE);
    assertThat(suite.skipFailedInvocationCounts()).isTrue();
    assertThat(suite.getPreserveOrder()).isFalse();
    assertThat(suite.getGroupByInstances()).isTrue();
    assertThat(suite.getAllowReturnValues()).isTrue();
    assertThat(suite.isShareThreadPoolForDataProviders()).isTrue();
    assertThat(suite.getLazyFactory()).isTrue();
    assertThat(suite.getParentModule()).isEqualTo("com.example.Module");
    assertThat(suite.getGuiceStage()).isEqualTo("PRODUCTION");
    assertThat(suite.getParameters()).containsOnly(entry("n", "42"), entry("s", "text"));
    assertThat(suite.getListeners()).containsExactly("com.example.Listener");
    assertThat(suite.getIncludedGroups()).containsExactly("in");
    assertThat(suite.getExcludedGroups()).containsExactly("out");
    assertThat(suite.getSuiteFiles()).containsExactly("child.yaml");
    assertThat(suite.getTests()).hasSize(1);

    XmlPackage xmlPackage = suite.getXmlPackages().get(0);
    assertThat(xmlPackage.getName()).isEqualTo("com.example.included");
    assertThat(xmlPackage.getInclude()).containsExactly("Foo");
    assertThat(xmlPackage.getExclude()).containsExactly("Bar");

    org.testng.xml.XmlMethodSelector selector = suite.getMethodSelectors().get(0);
    assertThat(selector.getClassName()).isEqualTo("com.example.Selector");
    assertThat(selector.getPriority()).isEqualTo(3);
  }

  @Test
  public void everyTestKeyIsRead() throws FileNotFoundException {
    XmlTest test =
        parse(
                "name: Schema",
                "tests:",
                "  - name: T",
                "    verbose: 4",
                "    parallel: classes",
                "    threadCount: 9",
                "    timeOut: 1234",
                "    preserveOrder: false",
                "    groupByInstances: true",
                "    allowReturnValues: true",
                "    skipFailedInvocationCounts: true",
                "    parameters: { p: 1 }",
                "    includedGroups: [ ti ]",
                "    excludedGroups: [ te ]",
                "    metaGroups: { all: [ ti, te ] }",
                "    dependencyGroups: { b: a }",
                "    methodSelectors:",
                "      - expression: groups.containsKey(\"x\")",
                "        language: beanshell",
                "    packages: [ com.example.pkg ]",
                "    classes:",
                "      - name: com.example.Klass",
                "        parameters: { cp: v }",
                "        includedMethods:",
                "          - name: m1",
                "            description: the first one",
                "            parameters: { ip: w }",
                "        excludedMethods: [ m2 ]")
            .getTests()
            .get(0);

    assertThat(test.getName()).isEqualTo("T");
    assertThat(test.getVerbose()).isEqualTo(4);
    assertThat(test.getParallel()).isEqualTo(XmlSuite.ParallelMode.CLASSES);
    assertThat(test.getThreadCount()).isEqualTo(9);
    assertThat(test.getTimeOut()).isEqualTo("1234");
    assertThat(test.getPreserveOrder()).isFalse();
    assertThat(test.getGroupByInstances()).isTrue();
    assertThat(test.getAllowReturnValues()).isTrue();
    assertThat(test.skipFailedInvocationCounts()).isTrue();
    assertThat(test.getLocalParameters()).containsOnly(entry("p", "1"));
    assertThat(test.getIncludedGroups()).containsExactly("ti");
    assertThat(test.getExcludedGroups()).containsExactly("te");
    assertThat(test.getMetaGroups()).containsOnly(entry("all", Arrays.asList("ti", "te")));
    assertThat(test.getXmlDependencyGroups()).containsOnly(entry("b", "a"));
    assertThat(test.getXmlPackages())
        .extracting(XmlPackage::getName)
        .containsExactly("com.example.pkg");

    org.testng.xml.XmlMethodSelector selector = test.getMethodSelectors().get(0);
    assertThat(selector.getScript().getExpression()).isEqualTo("groups.containsKey(\"x\")");
    assertThat(selector.getScript().getLanguage()).isEqualTo("beanshell");

    XmlClass xmlClass = test.getXmlClasses().get(0);
    assertThat(xmlClass.getName()).isEqualTo("com.example.Klass");
    assertThat(xmlClass.getLocalParameters()).containsOnly(entry("cp", "v"));
    assertThat(xmlClass.getExcludedMethods()).containsExactly("m2");

    XmlInclude include = xmlClass.getIncludedMethods().get(0);
    assertThat(include.getName()).isEqualTo("m1");
    assertThat(include.getDescription()).isEqualTo("the first one");
    assertThat(include.getLocalParameters()).containsOnly(entry("ip", "w"));
  }

  /**
   * A suite attribute added to {@link XmlSuite} used to become a YAML key on its own, because
   * snakeyaml derived the key set from the bean. It does not any more, so a new one has to be
   * declared here and written by {@link Yaml#toYaml} -- {@code lazy-factory} is the first to have
   * arrived since. Reading it back is what the schema alone does not prove.
   */
  @Test
  public void aSuiteAttributeAddedToTheModelSurvivesTheRoundTrip() throws FileNotFoundException {
    XmlSuite suite = parse("name: S", "lazyFactory: true");

    String emitted = Yaml.toYaml(suite).toString();

    assertThat(emitted).contains("lazyFactory: true");
    assertThat(parse(emitted.split("\n")).getLazyFactory()).isTrue();
  }

  /**
   * A parameter is text, as it is in {@code testng.xml}. Without the schema saying so, YAML
   * resolves an unquoted {@code 44.0} to a {@link Double} and the erased setter stores it in a
   * {@code Map<String, String>}, where it throws on the first caller that reads the map as strings.
   */
  @Test
  public void parametersAreReadAsText() throws FileNotFoundException {
    XmlSuite suite = parse("name: S", "parameters: { d: 44.0, b: true, i: 42, q: '7' }");

    assertThat(suite.getParameters())
        .containsOnly(entry("d", "44.0"), entry("b", "true"), entry("i", "42"), entry("q", "7"));
  }

  /**
   * The warning is the whole point of keeping the old spellings: they were kept rather than dropped
   * so that a suite file which uses one still runs while saying so. Asserting the text of {@link
   * YamlSchema#deprecationMessage} alone would pass just as well if nothing ever logged it, so the
   * document is parsed and what reaches the logger is read back.
   */
  @Test(dataProvider = "documentsUsingADeprecatedAlias")
  public void readingADeprecatedAliasWarns(
      String element, String deprecated, String canonical, String document)
      throws FileNotFoundException {
    PrintStream err = System.err;
    ByteArrayOutputStream logged = new ByteArrayOutputStream();
    System.setErr(new PrintStream(logged, true, StandardCharsets.UTF_8));
    try {
      parse(document.split("\n"));
    } finally {
      System.setErr(err);
    }

    assertThat(logged.toString(StandardCharsets.UTF_8))
        .contains(YamlSchema.deprecationMessage(element, deprecated, canonical));
  }

  @DataProvider
  public static Object[][] documentsUsingADeprecatedAlias() {
    return new Object[][] {
      {"suite", "xmlPackages", "packages", "name: S\nxmlPackages: [ a ]"},
      {"test", "xmlPackages", "packages", "name: S\ntests: [ { name: T, xmlPackages: [ a ] } ]"},
      {"test", "xmlClasses", "classes", "name: S\ntests: [ { name: T, xmlClasses: [ A ] } ]"},
      {
        "test",
        "xmlDependencyGroups",
        "dependencyGroups",
        "name: S\ntests: [ { name: T, xmlDependencyGroups: { a: b } } ]"
      },
      {
        "method selector",
        "name",
        "className",
        "name: S\nmethodSelectors: [ { name: com.example.Selector } ]"
      },
    };
  }

  @Test
  public void aDeprecatedAliasReadsLikeItsCanonicalKey() throws FileNotFoundException {
    XmlSuite deprecated =
        parse(
            "name: S",
            "xmlPackages: [ com.example.suite ]",
            "tests:",
            "  - name: T",
            "    xmlPackages: [ com.example.test ]",
            "    xmlDependencyGroups: { b: a }",
            "    xmlClasses: [ com.example.Klass ]",
            "    methodSelectors:",
            "      - name: com.example.Selector");
    XmlSuite canonical =
        parse(
            "name: S",
            "packages: [ com.example.suite ]",
            "tests:",
            "  - name: T",
            "    packages: [ com.example.test ]",
            "    dependencyGroups: { b: a }",
            "    classes: [ com.example.Klass ]",
            "    methodSelectors:",
            "      - className: com.example.Selector");

    assertThat(Yaml.toYaml(deprecated).toString()).isEqualTo(Yaml.toYaml(canonical).toString());
  }

  /**
   * A deprecated spelling and its canonical key write through the same property, so using both in
   * one mapping is a key declared twice however different the text looks. snakeyaml compares the
   * text, so {@code setAllowDuplicateKeys(false)} does not see it, and the second occurrence used
   * to overwrite the first without a word.
   */
  @DataProvider
  public static Object[][] bothSpellingsOfOneKey() {
    String inATest = "tests: [ { name: T, %s, %s } ]";
    return new Object[][] {
      {"suite", "packages", "packages: [ a ]", "xmlPackages: [ b ]", "%s\n%s"},
      {"test", "packages", "packages: [ a ]", "xmlPackages: [ b ]", inATest},
      {"test", "classes", "classes: [ A ]", "xmlClasses: [ B ]", inATest},
      {
        "test",
        "dependencyGroups",
        "dependencyGroups: { a: b }",
        "xmlDependencyGroups: { c: d }",
        inATest
      },
      {
        "method selector", "className", "className: A", "name: B", "methodSelectors: [ { %s, %s } ]"
      },
    };
  }

  @Test(dataProvider = "bothSpellingsOfOneKey")
  public void oneKeyUnderBothItsSpellingsIsRejected(
      String element,
      String canonical,
      String canonicalEntry,
      String deprecatedEntry,
      String template) {
    assertRepeatedKeyIsRejected(element, canonical, template, canonicalEntry, deprecatedEntry);
    // Both orders. Detection must not depend on which spelling the file happens to put first, and
    // the report names the two as they appear rather than always canonical first.
    assertRepeatedKeyIsRejected(element, canonical, template, deprecatedEntry, canonicalEntry);
  }

  private static void assertRepeatedKeyIsRejected(
      String element, String canonical, String template, String first, String second) {
    String document = "name: S\n" + String.format(template, first, second);

    assertThatThrownBy(() -> parse(document.split("\n")))
        .as("%s", document)
        .hasMessageContaining(
            YamlSchema.repeatedKeyMessage(element, canonical, keyOf(first), keyOf(second)));
  }

  private static String keyOf(String entry) {
    return entry.substring(0, entry.indexOf(':'));
  }

  /**
   * A merge key is the case a check on the raw mapping node cannot see: {@code <<: *base} brings
   * "packages" in from the anchor, and the spelling written next to it collides with a key that is
   * nowhere in the node's own text.
   */
  @Test
  public void oneKeyMergedInUnderItsOtherSpellingIsRejected() {
    assertThatThrownBy(
            () ->
                parse(
                    "name: S",
                    "tests:",
                    "  - &base",
                    "    name: T1",
                    "    packages: [ p1 ]",
                    "  - <<: *base",
                    "    name: T2",
                    "    xmlPackages: [ p2 ]"))
        .hasMessageContaining(
            YamlSchema.repeatedKeyMessage("test", "packages", "packages", "xmlPackages"));
  }

  /**
   * The keys the model used to expose by accident. {@code fileName} and {@code parsed} are parse
   * bookkeeping, {@code index} and {@code suite} are computed, {@code groups} could never work at
   * all because {@code XmlRun} has no setter for its includes, and {@code class} would have handed
   * snakeyaml a {@link Class} to build from a name.
   */
  @DataProvider
  public static Object[][] keysOutsideTheSchema() {
    return new Object[][] {
      {"suite", "fileName", "fileName: elsewhere.yaml"},
      {"suite", "parsed", "parsed: true"},
      {"suite", "parentSuite", "parentSuite: { name: P }"},
      {"suite", "groups", "groups: { run: { includes: [ a ] } }"},
      {"suite", "xmlMethodSelectors", "xmlMethodSelectors: { methodSelectors: [] }"},
      {"suite", "objectFactoryClass", "objectFactoryClass: com.example.Factory"},
      {"suite", "nosuchkey", "nosuchkey: 1"},
      {"test", "index", "tests: [ { name: T, index: 1 } ]"},
      {"test", "suite", "tests: [ { name: T, suite: { name: S } } ]"},
      {"test", "xmlSuite", "tests: [ { name: T, xmlSuite: { name: S } } ]"},
      {"test", "groups", "tests: [ { name: T, groups: { run: { includes: [ a ] } } } ]"},
      {"test", "script", "tests: [ { name: T, script: { language: beanshell } } ]"},
      {"test", "nosuchkey", "tests: [ { name: T, nosuchkey: 1 } ]"},
      {"class", "class", "tests: [ { name: T, classes: [ { name: C, class: C } ] } ]"},
      {"class", "index", "tests: [ { name: T, classes: [ { name: C, index: 1 } ] } ]"},
      {
        "class", "xmlTest", "tests: [ { name: T, classes: [ { name: C, xmlTest: { name: T } } ] } ]"
      },
      {"package", "xmlClasses", "packages: [ { name: p, xmlClasses: [] } ]"},
      {
        "include",
        "index",
        "tests: [ { name: T, classes: [ { name: C, includedMethods: [ { name: m, index: 1 } ] } ] } ]"
      },
      {
        "include",
        "invocationNumbers",
        "tests: [ { name: T, classes: [ { name: C, includedMethods: [ { name: m, invocationNumbers: [ 1 ] } ] } ] } ]"
      },
      {
        "method selector",
        "script",
        "methodSelectors: [ { className: c, script: { language: bsh } } ]"
      },
    };
  }

  @Test(dataProvider = "keysOutsideTheSchema")
  public void aKeyOutsideTheSchemaIsRejected(String element, String key, String document) {
    assertThatThrownBy(() -> parse("name: S", document))
        .hasMessageContaining("Unknown key \"" + key + "\" in a <" + element + ">")
        .hasMessageContaining("Accepted keys: ");
  }

  /**
   * The reason the {@code <class>} description exists at all. {@code Converter} and the failed
   * suite reporter read files whose classes are not on the classpath, and {@link XmlClass} resolves
   * the name in its constructor.
   */
  @Test
  public void aClassIsNotResolvedWhenClassesAreNotLoaded() {
    assertThatCode(
            () -> parse(false, "name: S", "tests:", "  - name: T", CLASSES_THAT_DO_NOT_EXIST))
        .doesNotThrowAnyException();

    assertThatThrownBy(
            () -> parse(true, "name: S", "tests:", "  - name: T", CLASSES_THAT_DO_NOT_EXIST))
        .hasRootCauseInstanceOf(TestNGException.class)
        .hasMessageContaining("Cannot find class in classpath");
  }

  /** Both shapes a class entry can take: the bare name, and the mapping that carries extras. */
  private static final String CLASSES_THAT_DO_NOT_EXIST =
      String.join(
          "\n",
          "    classes:",
          "      - com.example.Missing",
          "      - name: com.example.AlsoMissing",
          "        excludedMethods: [ m ]");

  @Test
  public void aClassEntryWithoutANameIsReported() {
    assertThatThrownBy(
            () -> parse("name: S", "tests:", "  - name: T", "    classes:", "      - index: 1"))
        .hasRootCauseInstanceOf(TestNGException.class)
        .hasMessageContaining("must be a name or carry a \"name\" key");
  }

  /** A name that is not a scalar reaches the same report rather than a class cast. */
  @Test
  public void aClassEntryWhoseNameIsNotAScalarIsReported() {
    assertThatThrownBy(
            () ->
                parse("name: S", "tests:", "  - name: T", "    classes:", "      - name: { a: b }"))
        .hasRootCauseInstanceOf(TestNGException.class)
        .hasMessageContaining("must be a name or carry a \"name\" key");
  }

  /** Likewise for a meta group that is not a list: the entry is named, not the types. */
  @Test
  public void aMetaGroupThatIsNotAListIsReported() {
    assertThatThrownBy(
            () -> parse("name: S", "tests:", "  - name: T", "    metaGroups: { all: nope }"))
        .hasRootCauseInstanceOf(TestNGException.class)
        .hasMessageContaining("The meta group \"all\" must list the groups it stands for");
  }

  /**
   * A repeated key is rejected rather than resolved to its last occurrence, which is what snakeyaml
   * does by default: a suite file that declares {@code tests} twice would silently run half of what
   * it says.
   */
  @Test
  public void aKeyDeclaredTwiceIsRejected() {
    assertThatThrownBy(() -> parse("name: S", "verbose: 1", "verbose: 2"))
        .hasMessageContaining("found duplicate key verbose");
  }

  /** Guards the sets above against a copy-paste that declares the same key twice. */
  @Test
  public void noElementDeclaresAKeyTwice() {
    for (YamlSchema.SchemaType<?> type :
        Arrays.asList(
            YamlSchema.suite(),
            YamlSchema.test(),
            YamlSchema.xmlClass(false),
            YamlSchema.xmlPackage(),
            YamlSchema.include(),
            YamlSchema.methodSelector())) {
      assertThat(type.aliases().keySet())
          .as("a deprecated alias of %s must not also be a canonical key", type.getType())
          .doesNotContainAnyElementsOf(type.acceptedKeys());
    }
  }

  private static XmlSuite parse(String... lines) throws FileNotFoundException {
    return parse(false, lines);
  }

  private static XmlSuite parse(boolean loadClasses, String... lines) throws FileNotFoundException {
    byte[] document = String.join("\n", lines).getBytes(StandardCharsets.UTF_8);
    return Yaml.parse("schema.yaml", new ByteArrayInputStream(document), loadClasses);
  }
}
