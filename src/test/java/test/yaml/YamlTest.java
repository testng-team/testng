package test.yaml;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.Yaml;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;
import org.xml.sax.SAXException;

import test.SimpleBaseTest;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import junit.framework.Assert;

public class YamlTest extends SimpleBaseTest {

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
//      new Object[] { "a1" },
      new Object[] { "a3" },
    };
  }

//  @Test(dataProvider = "dp")
//  public void compareFiles(String name)
  @Test
  public void compareFiles()
      throws ParserConfigurationException, SAXException, IOException {
    String name = "a3";
    Collection<XmlSuite> s1 =
        Yaml.parse(getPathToResource("yaml" + File.separator + name + ".yaml"));
    Collection<XmlSuite> s2 =
        new Parser(getPathToResource("yaml" + File.separator + name + ".xml")).parse();

    Assert.assertEquals(s1, s2);
  }
}
