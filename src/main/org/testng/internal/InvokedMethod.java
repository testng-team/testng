package org.testng.internal;

import java.io.Serializable;

import org.testng.IInvokedMethod;
import org.testng.ITestNGMethod;

public class InvokedMethod implements Serializable, IInvokedMethod {
  transient private Object m_instance;
  private ITestNGMethod m_testMethod;
  private Object[] m_parameters;
  private boolean m_isTest = true;
  private boolean m_isConfigurationMethod = false;
  private long m_date = System.currentTimeMillis();
  
  /**
   * @param m_object
   * @param m_method
   * @param m_parameters
   */
  public InvokedMethod(Object instance, 
                       ITestNGMethod method, 
                       Object[] parameters,
                       boolean isTest, 
                       boolean isConfiguration, 
                       long date) {
    m_instance = instance;
    m_testMethod = method;
    m_parameters = parameters;
    m_isTest = isTest;
    m_isConfigurationMethod = isConfiguration;
    m_date = date;
  }
  
  /* (non-Javadoc)
   * @see org.testng.internal.IInvokedMethod#isTestMethod()
   */
  public boolean isTestMethod() {
    return m_isTest;
  }

  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(m_testMethod.toString());
    for (Object p : m_parameters) {
      result.append(p).append(" ");
    }
    result.append(" ").append(m_instance.hashCode());
    
    return result.toString();
  }
  
  /* (non-Javadoc)
   * @see org.testng.internal.IInvokedMethod#isConfigurationMethod()
   */
  public boolean isConfigurationMethod() {
    return m_isConfigurationMethod;
  }
  
  /* (non-Javadoc)
   * @see org.testng.internal.IInvokedMethod#getTestMethod()
   */
  public ITestNGMethod getTestMethod() {
    return m_testMethod;
  }

  /* (non-Javadoc)
   * @see org.testng.internal.IInvokedMethod#getDate()
   */
  public long getDate() {
    return m_date;
  }

}
