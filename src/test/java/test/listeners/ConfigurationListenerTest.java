package test.listeners;

import org.testng.Assert;
import org.testng.IConfigurationListener2;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class ConfigurationListenerTest extends SimpleBaseTest {

  static public class CL implements IConfigurationListener2 {

    private static int m_status = 0;

    @Override
    public void beforeConfiguration(ITestResult tr) {
      m_status += 1;
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
      m_status += 3;
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
      m_status += 5;
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
      m_status += 7;
    }

  }

  private void runTest(Class<?> cls, int expected) {
    TestNG tng = create(cls);
    CL listener = new CL();
    CL.m_status = 0;
    tng.addListener(listener);
    tng.run();

    Assert.assertEquals(CL.m_status, expected);
  }

  @Test
  public void shouldSucceed() {
    runTest(ConfigurationListenerSucceedSampleTest.class, 1 + 3);
  }

  @Test
  public void shouldFail() {
    runTest(ConfigurationListenerFailSampleTest.class, 1 + 5);
  }

  @Test
  public void shouldSkip() {
    runTest(ConfigurationListenerSkipSampleTest.class, 1 + 5 + 7); // fail + skip
  }
}
