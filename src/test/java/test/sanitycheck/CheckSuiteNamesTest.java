package test.sanitycheck;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.Arrays;

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
  @Test(expectedExceptions = TestNGException.class, expectedExceptionsMessageRegExp = "\\s*Two suites cannot have the same name.*")
  public void checkChildSuitesFails() {
    TestNG tng = create();
    String testngXmlPath = getPathToResource("sanitycheck/test-s-a.xml");
    tng.setTestSuites(Arrays.asList(testngXmlPath));
    tng.run();
  }

  /**
   * Checks that suites created programmatically also fails as expected
   */
  @Test(expectedExceptions = TestNGException.class, expectedExceptionsMessageRegExp = "\\s*Two suites cannot have the same name.*")
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
  }
}
