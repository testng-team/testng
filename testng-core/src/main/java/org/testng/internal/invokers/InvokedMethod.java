package org.testng.internal.invokers;

import org.testng.IInvokedMethod;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

public class InvokedMethod implements IInvokedMethod {

  private final long m_date;
  private final ITestResult m_testResult;

  public InvokedMethod(long date, ITestResult testResult) {
    m_date = date;
    m_testResult = testResult;
  }

  /* (non-Javadoc)
   * @see org.testng.internal.IInvokedMethod#isTestMethod()
   */
  @Override
  public boolean isTestMethod() {
    return m_testResult.getMethod().isTest();
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder().append(m_testResult.getMethod());
    for (Object p : m_testResult.getParameters()) {
      result.append(p).append(" ");
    }
    Object instance = m_testResult.getInstance();
    result.append(" ").append(instance != null ? instance.hashCode() : " <static>");

    return result.toString();
  }

  /* (non-Javadoc)
   * @see org.testng.internal.IInvokedMethod#isConfigurationMethod()
   */
  @Override
  public boolean isConfigurationMethod() {
    return TestNgMethodUtils.isConfigurationMethod(m_testResult.getMethod());
  }

  /* (non-Javadoc)
   * @see org.testng.internal.IInvokedMethod#getTestMethod()
   */
  @Override
  public ITestNGMethod getTestMethod() {
    return m_testResult.getMethod();
  }

  /* (non-Javadoc)
   * @see org.testng.internal.IInvokedMethod#getDate()
   */
  @Override
  public long getDate() {
    return m_date;
  }

  @Override
  public ITestResult getTestResult() {
    return m_testResult;
  }
}
