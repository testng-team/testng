package test.dataprovider;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Make sure that if a test method fails in the middle of a data provider, the rest
 * of the data set is still run.
 */
public class FailedDataProviderTest {
  static int m_total = 0;

  @BeforeMethod
  public void init() {
    m_total = 0;
  }

  @Test
  public void allMethodsShouldBeInvoked() {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] { FailedDataProviderSample.class });
    tng.setVerbose(0);
    tng.run();

    Assert.assertEquals(m_total, 6);
  }
}

