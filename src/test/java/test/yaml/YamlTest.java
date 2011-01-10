package test.yaml;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;
import org.xml.sax.SAXException;

import test.SimpleBaseTest;

import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

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
  public void compareFiles(String name)
      throws ParserConfigurationException, SAXException, IOException {
    Collection<XmlSuite> s1 =
        new Parser(getPathToResource("yaml" + File.separator + name + ".yaml")).parse();
    Collection<XmlSuite> s2 =
        new Parser(getPathToResource("yaml" + File.separator + name + ".xml")).parse();

    Assert.assertEquals(s1, s2);
  }
}
