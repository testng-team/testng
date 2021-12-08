package test.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.Yaml;
import org.testng.internal.YamlParser;
import org.testng.reporters.Files;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlSuite;
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

  @Test(dataProvider = "dp")
  public void compareFiles(String name) throws IOException {
    Collection<XmlSuite> s1 =
        new Parser(getPathToResource("yaml" + File.separator + name + ".yaml")).parse();
    Collection<XmlSuite> s2 =
        new Parser(getPathToResource("yaml" + File.separator + name + ".xml")).parse();

    Assert.assertEquals(s1, s2);
  }

  @Test(description = "GITHUB-1787")
  public void testParameterInclusion() throws IOException {
    SuiteXmlParser parser = new SuiteXmlParser();
    String file = "src/test/resources/yaml/1787.xml";
    XmlSuite xmlSuite = parser.parse(file, new FileInputStream(file), false);
    StringBuilder yaml = org.testng.internal.Yaml.toYaml(xmlSuite);
    Matcher m = Pattern.compile("parameters:").matcher(yaml.toString());
    int count = 0;
    while (m.find()) {
      count++;
    }
    assertThat(count).isEqualTo(5);
    File newSuite = File.createTempFile("suite", ".xml");
    newSuite.deleteOnExit();
    Files.writeFile(yaml.toString(), newSuite);
    assertThat(parser.parse(newSuite.getAbsolutePath(), new FileInputStream(file), false))
        .isEqualTo(xmlSuite);
  }

  @Test(description = "GITHUB-2078")
  public void testXmlDependencyGroups() throws IOException {
    String actualXmlFile = "src/test/resources/yaml/2078.xml";
    XmlSuite actualXmlSuite =
        new SuiteXmlParser().parse(actualXmlFile, new FileInputStream(actualXmlFile), false);
    String expectedYamlFile = "src/test/resources/yaml/2078.yaml";
    String expectedYaml =
        new String(
            java.nio.file.Files.readAllBytes(Paths.get(expectedYamlFile)), StandardCharsets.UTF_8);
    assertThat(Yaml.toYaml(actualXmlSuite).toString()).isEqualToNormalizingNewlines(expectedYaml);
  }

  @Test(description = "GITHUB-2689")
  public void testLoadClassesFlag() throws IOException {
    YamlParser yamlParser = new YamlParser();
    String yamlSuiteFile = "src/test/resources/yaml/suiteWithNonExistentTest.yaml";

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

  private Throwable getRootCause(Throwable throwable) {
    return throwable.getCause() != null ? getRootCause(throwable.getCause()) : throwable;
  }
}
