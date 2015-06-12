package org.testng.internal;

import java.io.Serializable;

import org.testng.IInvokedMethod;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

public class InvokedMethod implements Serializable, IInvokedMethod {
  private static final long serialVersionUID = 2126127194102819222L;
  transient private Object m_instance;
  private ITestNGMethod m_testMethod;
  private Object[] m_parameters;
  private long m_date = System.currentTimeMillis();
  private ITestResult m_testResult;

  public InvokedMethod(Object instance,
                       ITestNGMethod method,
                       Object[] parameters,
                       long date,
                       ITestResult testResult) {
    m_instance = instance;
    m_testMethod = method;
    m_parameters = parameters;
    m_date = date;
    m_testResult = testResult;
  }

  /* (non-Javadoc)
   * @see org.testng.internal.IInvokedMethod#isTestMethod()
   */
  @Override
  public boolean isTestMethod() {
    return m_testMethod.isTest();
  }

  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(m_testMethod.toString());
    for (Object p : m_parameters) {
      result.append(p).append(" ");
    }
    result.append(" ").append(m_instance != null ? m_instance.hashCode() : " <static>");

    return result.toString();
  }

  /* (non-Javadoc)
   * @see org.testng.internal.IInvokedMethod#isConfigurationMethod()
   */
  @Override
  public boolean isConfigurationMethod() {
    return m_testMethod.isBeforeMethodConfiguration() ||
           m_testMethod.isAfterMethodConfiguration() ||
           m_testMethod.isBeforeTestConfiguration() ||
           m_testMethod.isAfterTestConfiguration() ||
           m_testMethod.isBeforeClassConfiguration() ||
           m_testMethod.isAfterClassConfiguration() ||
           m_testMethod.isBeforeSuiteConfiguration() ||
           m_testMethod.isAfterSuiteConfiguration();
  }

  /* (non-Javadoc)
   * @see org.testng.internal.IInvokedMethod#getTestMethod()
   */
  @Override
  public ITestNGMethod getTestMethod() {
    return m_testMethod;
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
