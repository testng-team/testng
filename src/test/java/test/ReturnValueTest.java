package test;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Arrays;

public class ReturnValueTest extends SimpleBaseTest {

  private XmlSuite m_suite;
  private XmlTest m_test;

  @BeforeMethod
  public void before() {
    m_suite = createXmlSuite("suite");
    m_test = createXmlTest(m_suite, "test", ReturnValueSampleTest.class.getName());
  }

  @Test
  public void suiteReturnValueTestShouldBeRun() {
    m_suite.setAllowReturnValues(true);
    runTest(true);
  }
  
  @Test
  public void suiteReturnValueTestShouldNotBeRun() {
    runTest(false);
  }

  @Test
  public void testReturnValueTestShouldBeRun() {
    m_test.setAllowReturnValues(true);
    runTest(true);
  }
  
  private void runTest(boolean allowed) {
    TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(m_suite));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    if (allowed) {
      Assert.assertEquals(tla.getFailedTests().size(), 0);
      Assert.assertEquals(tla.getSkippedTests().size(), 0);
      assertTestResultsEqual(tla.getPassedTests(), Arrays.asList("shouldRun"));
    } else {
      Assert.assertEquals(tla.getFailedTests().size(), 0);
      Assert.assertEquals(tla.getPassedTests().size(), 0);
      Assert.assertEquals(tla.getSkippedTests().size(), 0);
    }
  }
  
}
