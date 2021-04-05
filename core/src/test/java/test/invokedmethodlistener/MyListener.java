package test.invokedmethodlistener;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class MyListener implements IInvokedMethodListener {
  private int m_beforeCount = 0;
  private int m_afterCount = 0;

  private Throwable suiteThrowable;
  private int suiteStatus = 0;
  private Throwable methodThrowable;
  private int methodStatus = 0;

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    m_afterCount++;
    if (method.getTestMethod().isAfterSuiteConfiguration()) {
      suiteStatus = testResult.getStatus();
      suiteThrowable = testResult.getThrowable();
    }
    if (method.getTestMethod().isTest()) {
      methodStatus = testResult.getStatus();
      methodThrowable = testResult.getThrowable();
    }
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    m_beforeCount++;
  }

  public int getBeforeCount() {
    return m_beforeCount;
  }

  public int getAfterCount() {
    return m_afterCount;
  }

  public Throwable getSuiteThrowable() {
    return suiteThrowable;
  }

  public int getSuiteStatus() {
    return suiteStatus;
  }

  public Throwable getMethodThrowable() {
    return methodThrowable;
  }

  public int getMethodStatus() {
    return methodStatus;
  }

}
