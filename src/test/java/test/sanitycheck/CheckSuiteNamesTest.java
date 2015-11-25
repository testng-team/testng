package test.sanitycheck;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.Parser;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.xml.sax.SAXException;
import test.SimpleBaseTest;
import java.io.IOException;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;

public class CheckSuiteNamesTest extends SimpleBaseTest {

  /**
   * Child suites have different names
   */
  @Test
  public void checkChildSuites() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    String testngXmlPath = getPathToResource("sanitycheck/test-s-b.xml");
    tng.setTestSuites(Arrays.asList(testngXmlPath));
    tng.addListener(tla);
    tng.run();
    Assert.assertEquals(tla.getPassedTests().size(), 4);
  }

  /**
   * Child suites have same names
   */
  @Test
  public void checkChildSuitesFails() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    String testngXmlPath = getPathToResource("sanitycheck/test-s-a.xml");
    tng.setTestSuites(Arrays.asList(testngXmlPath));
    tng.addListener(tla);
    tng.run();
    Assert.assertEquals(tla.getTestContexts().get(0).getSuite().getName(), "SanityCheck suites");
    Assert.assertEquals(tla.getTestContexts().get(1).getSuite().getName(), "SanityCheck suites");
    Assert.assertEquals(tla.getTestContexts().get(2).getSuite().getName(), "SanityCheck suites (0)");
    Assert.assertEquals(tla.getTestContexts().get(3).getSuite().getName(), "SanityCheck suites (0)");
  }

  /**
   * Checks that suites created programmatically also works as expected
   */
  @Test
  public void checkProgrammaticSuitesFails() {
    XmlSuite xmlSuite1 = new XmlSuite();
    xmlSuite1.setName("SanityCheckSuite");
    {
      XmlTest result = new XmlTest(xmlSuite1);
      result.getXmlClasses().add(new XmlClass(SampleTest1.class.getCanonicalName()));
    }

    XmlSuite xmlSuite2 = new XmlSuite();
    xmlSuite2.setName("SanityCheckSuite");
    {
      XmlTest result = new XmlTest(xmlSuite2);
      result.getXmlClasses().add(new XmlClass(SampleTest2.class.getCanonicalName()));
    }

    TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(xmlSuite1, xmlSuite2));
    tng.run();
    Assert.assertEquals(xmlSuite1.getName(), "SanityCheckSuite");
    Assert.assertEquals(xmlSuite2.getName(), "SanityCheckSuite (0)");
  }
  
  @Test
  public void checkXmlSuiteAddition() throws ParserConfigurationException, SAXException, IOException {
    TestNG tng = create();
    String testngXmlPath = getPathToResource("sanitycheck/test-s-b.xml");
    Parser parser = new Parser(testngXmlPath);	
    tng.setXmlSuites(parser.parseToList());
    tng.initializeSuitesAndJarFile();		
  }
}
