package test.invokedmethodlistener;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class MyListener implements IInvokedMethodListener {
  private int m_beforeCount;
  private int m_afterCount;

  private Throwable suiteThrowable;
  private int suiteStatus;
  private Throwable methodThrowable;
  private int methodStatus;

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
