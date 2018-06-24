package org.testng.xml;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

public class ParserTest {

  private static final String XML_FILE_NAME = "src/test/resources/a.xml";

  @Test(dataProvider = "dp")
  public void testParsing(String file) throws Exception {
    Parser parser = new Parser(file);
    List<XmlSuite> suites = parser.parseToList();
    assertEquals(suites.size(), 1);
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {
      {XML_FILE_NAME},
      {new File(XML_FILE_NAME).toURI().toString()},
      {"http://localhost:4444/testng.xml"},
      {"https://localhost:4444/testng.xml"}
    };
  }
}
