package test.sanitycheck;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

public class CheckTestNamesTest extends SimpleBaseTest {

  /**
   * Child suites and same suite has two tests with same name
   */
  @Test
  public void checkWithChildSuites() {
    runSuite("sanitycheck/test-a.xml");
  }

  /**
   * Simple suite with two tests with same name
   */
  @Test
  public void checkWithoutChildSuites() {
    runSuite("sanitycheck/test1.xml");
  }

  private void runSuite(String suitePath)
  {
    TestListenerAdapter tla = new TestListenerAdapter();
    boolean exceptionRaised = false;
    try {
      TestNG tng = create();
      String testngXmlPath = getPathToResource(suitePath);
      tng.setTestSuites(Arrays.asList(testngXmlPath));
      tng.addListener(tla);
      tng.run();
    } catch (TestNGException ex) {
      exceptionRaised = true;
      Assert.assertEquals(tla.getPassedTests().size(), 0);
      Assert.assertEquals(tla.getFailedTests().size(), 0);
    }
    Assert.assertTrue(exceptionRaised);
  }

  /**
   * Simple suite with no two tests with same name
   */
  @Test
  public void checkNoError() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    String testngXmlPath = getPathToResource("sanitycheck/test2.xml");
    tng.setTestSuites(Arrays.asList(testngXmlPath));
    tng.addListener(tla);
    tng.run();
    Assert.assertEquals(tla.getPassedTests().size(), 2);
  }

  /**
   * Child suites and tests within different suites have same names
   */
  @Test(enabled = false)
  public void checkNoErrorWtihChildSuites() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    String testngXmlPath = getPathToResource("sanitycheck/test-b.xml");
    tng.setTestSuites(Arrays.asList(testngXmlPath));
    tng.addListener(tla);
    tng.run();
    Assert.assertEquals(tla.getPassedTests().size(), 4);
  }

  /**
   * Checks that suites created programmatically also run as expected
   */
  @Test
  public void checkTestNamesForProgrammaticSuites() {
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("SanityCheckSuite");
    XmlTest result = new XmlTest(xmlSuite);
    result.getXmlClasses().add(new XmlClass(SampleTest1.class.getCanonicalName()));
    result = new XmlTest(xmlSuite);
    result.getXmlClasses().add(new XmlClass(SampleTest2.class.getCanonicalName()));

    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setXmlSuites(Arrays.asList(new XmlSuite[] { xmlSuite }));
    tng.run();
  }
}
