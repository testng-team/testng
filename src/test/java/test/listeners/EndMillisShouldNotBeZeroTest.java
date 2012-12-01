package test.listeners;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import test.listeners.EndMillisShouldNotBeZeroTest.MyInvokedMethodListener;
import junit.framework.Assert;

@Listeners(MyInvokedMethodListener.class)
public class EndMillisShouldNotBeZeroTest {
  private static long m_end;

  public static class MyInvokedMethodListener implements IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
      m_end = testResult.getEndMillis();
    }
    
  }

  @BeforeClass
  public void bm() {
    m_end = 0;
  }

  @Test
  public void f1()
  {
    try {
      Thread.sleep(1);
    } catch (InterruptedException handled) {
      Thread.currentThread().interrupt();
    }
  }

  @Test(description = "Make sure that ITestResult#getEndMillis is properly set",
      dependsOnMethods = "f1")
  public void f2() {
    Assert.assertTrue(m_end > 0);
  }
}
