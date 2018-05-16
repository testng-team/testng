package test.yaml;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.Files;
import org.testng.xml.Parser;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlSuite;

import test.SimpleBaseTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class YamlTest extends SimpleBaseTest {

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] { "a1" },
      new Object[] { "a2" },
      new Object[] { "a3" },
      new Object[] { "a4" },
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

}
