package test;

import java.util.ArrayList;
import java.util.List;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class TestHelper {

  public static XmlSuite createSuite(String cls, String suiteName) {
    XmlSuite result = new XmlSuite();
    result.setName(suiteName);
    
    XmlTest test = new XmlTest(result);
    test.setName("TmpTest");
    List<XmlClass> classes = new ArrayList<XmlClass>();
    classes.add(new XmlClass(cls));
    test.setClassNames(classes);
    
    return result;
  }

  public static TestNG createTestNG(XmlSuite suite) {
    return createTestNG(suite, System.getProperty("java.io.tmpdir"));
  }

  public static TestNG createTestNG(XmlSuite suite, String outputDir) {
    TestNG result = new TestNG();
    List<XmlSuite> suites = new ArrayList<XmlSuite>();
    suites.add(suite);
    result.setXmlSuites(suites);
    result.setOutputDirectory(outputDir);
    result.setVerbose(0);
    
    return result;
  }

}
