package org.testng.xml;

import static org.assertj.core.api.Assertions.fail;
import static test.SimpleBaseTest.getPathToResource;

import java.io.File;
import java.io.FileInputStream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SuiteXmlParserTest {

  private static final File PARENT = new File(getPathToResource("xml"));

  @DataProvider
  private static Object[][] dp() {
    return new Object[][] {
      {"goodWithDoctype.xml", true},
      {"goodWithoutDoctype.xml", true},
      {"badWithDoctype.xml", false}, // TestNGException -> SAXParseException
      {"badWithoutDoctype.xml", false}, // NullPointerException
      {"issue174.xml", true}
    };
  }

  @Test(dataProvider = "dp")
  public void testParse(String fileName, boolean shouldWork) {
    SuiteXmlParser parser = new SuiteXmlParser();

    try (FileInputStream stream = new FileInputStream(new File(PARENT, fileName))) {
      XmlSuite suite = parser.parse(fileName, stream, false);
      if (!shouldWork) {
        fail("Parsing of " + fileName + " is supposed to fail");
      }
    } catch (Exception e) {
      if (shouldWork) {
        fail("Parsing of " + fileName + " is supposed to work");
      }
    }
  }
}
