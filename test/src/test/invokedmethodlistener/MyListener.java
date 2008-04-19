package test.invokedmethodlistener;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class MyListener implements IInvokedMethodListener {
  private int m_beforeCount = 0;
  private int m_afterCount = 0;

  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    m_afterCount++;
  }

  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    m_beforeCount++;
  }
  
  public int getBeforeCount() {
    return m_beforeCount;
  }
  
  public int getAfterCount() {
    return m_afterCount;
  }

  
}
