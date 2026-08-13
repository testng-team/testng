package test.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.testng.TestNGException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.Yaml;
import org.testng.internal.YamlParser;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.testng.xml.internal.Parser;
import test.SimpleBaseTest;

public class YamlTest extends SimpleBaseTest {
  public static final String CLASS_NOT_FOUND_MESSAGE = "Cannot find class in classpath";

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] {"a1"}, new Object[] {"a2"}, new Object[] {"a3"}, new Object[] {"a4"},
    };
  }

  @Test(
      description =
          "Validate that the YamlParser accepts yaml files with a .yaml or a .yml file extension, but not other file types.")
  public void accept() {
    YamlParser yamlParser = new YamlParser();
    assertThat(yamlParser.accept("TestSuite.yml")).isTrue();
    assertThat(yamlParser.accept("TestSuite.yaml")).isTrue();
    assertThat(yamlParser.accept("TestSuite.xml")).isFalse();
  }

  @Test(dataProvider = "dp")
  public void compareFiles(String name) throws IOException {
    Collection<XmlSuite> s1 = parseSuiteFile(name + ".yaml");
    Collection<XmlSuite> s2 = parseSuiteFile(name + ".xml");

    assertThat(s1).isEqualTo(s2);
  }

  /**
   * Classes are not resolved, because the fixtures name classes of TestNG's own test suite and this
   * module does not carry them. Nothing is lost: {@code XmlClass.equals} compares the name, never
   * the resolved {@link Class}, so what {@code compareFiles} asserts is unchanged.
   */
  private static Collection<XmlSuite> parseSuiteFile(String fileName) throws IOException {
    Parser parser = new Parser(getPathToResource("yaml" + File.separator + fileName));
    parser.setLoadClasses(false);
    return parser.parse();
  }

  @Test(description = "GITHUB-1787")
  public void testParameterInclusion() throws IOException {
    String file = getPathToResource("yaml/1787.xml");
    XmlSuite xmlSuite = new SuiteXmlParser().parse(file, new FileInputStream(file), false);

    XmlSuite reparsed = parseYaml(file, Yaml.toYaml(xmlSuite).toString());

    assertThat(reparsed.getParameters()).containsEntry("suiteLevel", "suiteValue");
    XmlTest test = reparsed.getTests().get(0);
    assertThat(test.getLocalParameters()).containsEntry("testLevel", "testValue");
    assertThat(test.getClasses().get(0).getIncludedMethods())
        .extracting(include -> include.getLocalParameters().get("teqUid"))
        .containsExactly("Teq1", "Teq2", "Teq3");
  }

  @Test(description = "GITHUB-2078")
  public void testXmlDependencyGroups() throws IOException {
    String actualXmlFile = getPathToResource("yaml/2078.xml");
    XmlSuite actualXmlSuite =
        new SuiteXmlParser().parse(actualXmlFile, new FileInputStream(actualXmlFile), false);
    String expectedYamlFile = getPathToResource("yaml/2078.yaml");
    String expectedYaml =
        new String(Files.readAllBytes(Paths.get(expectedYamlFile)), StandardCharsets.UTF_8);

    String actualYaml = Yaml.toYaml(actualXmlSuite).toString();

    assertThat(actualYaml).isEqualToNormalizingNewlines(expectedYaml);
    // The golden file cannot make this distinction on its own: folding the line at the first of
    // the two spaces would collapse them, and the result would still read as a plausible list of
    // dependencies.
    assertThat(parseYaml(actualXmlFile, actualYaml).getTests().get(0).getXmlDependencyGroups())
        .containsEntry("c", "a  b");
  }

  /**
   * A suite level {@code <define>} has no YAML key: {@code XmlSuite} exposes no {@code metaGroups}
   * property, unlike {@code XmlTest}. Writing one anyway produced a file the reader rejects
   * outright, and no YAML fixture can cover it because no YAML fixture can declare one.
   */
  @Test
  public void suiteLevelMetaGroupsAreNotWritten() throws IOException {
    String file = getPathToResource("xml/suite-level-groups.xml");
    XmlSuite xmlSuite = new SuiteXmlParser().parse(file, new FileInputStream(file), false);

    XmlSuite reparsed = parseYaml(file, Yaml.toYaml(xmlSuite).toString());

    assertThat(reparsed.getIncludedGroups()).containsExactly("PlatformTests");
  }

  @Test(description = "GITHUB-2689")
  public void testLoadClassesFlag() throws IOException {
    YamlParser yamlParser = new YamlParser();
    String yamlSuiteFile = getPathToResource("yaml/suiteWithNonExistentTest.yaml");

    try {
      yamlParser.parse(yamlSuiteFile, new FileInputStream(yamlSuiteFile), false);
    } catch (Throwable throwable) {
      Throwable rootCause = getRootCause(throwable);
      String rootCauseMessage = rootCause.getMessage();
      if (rootCauseMessage.contains(CLASS_NOT_FOUND_MESSAGE)) {
        throw new AssertionError("TestNG shouldn't attempt to load test class", throwable);
      }

      throw new AssertionError("Yaml parser failed to parse suite", throwable);
    }
  }

  /**
   * A YAML suite must fail the way an XML one does. {@code ISuiteParser} declares {@code
   * TestNGException} and {@code SuiteXmlParser} wraps its SAX failures, so a snakeyaml error
   * escaping raw would leave callers with two contracts to handle.
   */
  @Test
  public void aSuiteOutsideTheSchemaIsReportedAsATestNGException() {
    String document = "name: S\nfileName: elsewhere.yaml\n";

    assertThatThrownBy(
            () ->
                new YamlParser()
                    .parse(
                        "schema.yaml",
                        new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8)),
                        false))
        .isInstanceOf(TestNGException.class)
        .hasMessageContaining("Unknown key \"fileName\" in a <suite>");
  }

  @Test(description = "GITHUB-2857")
  public void testXmlTestIndex() throws IOException {
    YamlParser yamlParser = new YamlParser();
    String yamlSuiteFile = getPathToResource("yaml/testXmlTestIndex.yaml");
    XmlSuite suite = yamlParser.parse(yamlSuiteFile, new FileInputStream(yamlSuiteFile), false);
    List<XmlTest> tests = suite.getTests();
    assertThat(tests.size()).isEqualTo(3);
    for (int i = 0; i < tests.size(); i++) {
      assertThat(tests.get(i).getIndex()).isEqualTo(i);
    }
  }

  private static XmlSuite parseYaml(String fileName, String yaml) throws FileNotFoundException {
    byte[] bytes = yaml.getBytes(StandardCharsets.UTF_8);
    return Yaml.parse(fileName, new ByteArrayInputStream(bytes), false);
  }

  private Throwable getRootCause(Throwable throwable) {
    return throwable.getCause() != null ? getRootCause(throwable.getCause()) : throwable;
  }
}
