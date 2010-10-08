package test.dataprovider;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import test.BaseTest;

public class FailedDataProviderTest extends BaseTest {
  static int m_total = 0;

  @BeforeMethod
  public void init() {
    m_total = 0;
  }

  /**
   * Make sure that if a test method fails in the middle of a data provider, the rest
   * of the data set is still run.
   */
  @Test
  public void allMethodsShouldBeInvoked() {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] { FailedDataProviderSample.class });
    tng.setVerbose(0);
    tng.run();

    Assert.assertEquals(m_total, 6);
  }

  @Test
  public void failedDataProviderShouldCauseSkip() {
    addClass("test.dataprovider.DependentSampleTest");

    run();
    String[] passed = {
      "method1"
    };
    String[] failed = {
      "method1"
    };
    String[] skipped = {
      "method2"
    };
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }
}

