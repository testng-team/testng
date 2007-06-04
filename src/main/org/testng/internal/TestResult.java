package org.testng.internal;

import java.util.List;

import org.testng.IClass;
import org.testng.ITest;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;


/**
 * This class represents the result of a test.
 *
 * @author Cedric Beust, May 2, 2004
 */
public class TestResult implements ITestResult {

  private IClass m_testClass = null;
  private ITestNGMethod m_method = null;
  private int m_status = -1;
  private Throwable m_throwable = null;
  private long m_startMillis = 0;
  private long m_endMillis = 0;
  private String m_name = null;
  private String m_host;
  private Object[] m_parameters = {};

  /**
   * 
   * @param testClass
   * @param instance
   * @param method
   * @param throwable
   * @param start
   * @param end
   */
  public TestResult(IClass testClass,
                    Object instance,
                    ITestNGMethod method,
                    Throwable throwable,
                    long start,
                    long end)
  {
    m_testClass = testClass;
    m_throwable = throwable;
    if (null == m_throwable) {
      m_status = ITestResult.SUCCESS;
    }
    m_startMillis = start;
    m_endMillis = end;
    m_method = method;
    
    //
    // Assign a name if the instance is an instanceof ITest
    //
    m_name = m_method.getMethod().getName();
    if (null != instance && (instance instanceof ITest)) {
      m_name += " (" + ((ITest) instance).getTestName() + ")";
    }
  }
  
  private static void ppp(String s) {
    System.out.println("[TestResult] " + s);
  }

  public void setEndMillis(long millis) {
    m_endMillis = millis;
  }

  public String getName() {
    return m_name;
  }

  /**
   * @return Returns the method.
   */
  public ITestNGMethod getMethod() {
    return m_method;
  }

  /**
   * @param method The method to set.
   */
  public void setMethod(ITestNGMethod method) {
    m_method = method;
  }

  /**
   * @return Returns the status.
   */
  public int getStatus() {
    return m_status;
  }

  /**
   * @param status The status to set.
   */
  public void setStatus(int status) {
    m_status = status;
  }

  public boolean isSuccess() {
    return ITestResult.SUCCESS == m_status;
  }

  /**
   * @return Returns the testClass.
   */
  public IClass getTestClass() {
    return m_testClass;
  }

  /**
   * @param testClass The testClass to set.
   */
  public void setTestClass(IClass testClass) {
    m_testClass = testClass;
  }

  /**
   * @return Returns the throwable.
   */
  public Throwable getThrowable() {
    return m_throwable;
  }

  /**
   * @param throwable The throwable to set.
   */
  public void setThrowable(Throwable throwable) {
    m_throwable = throwable;
  }

  /**
   * @return Returns the endMillis.
   */
  public long getEndMillis() {
    return m_endMillis;
  }

  /**
   * @return Returns the startMillis.
   */
  public long getStartMillis() {
    return m_startMillis;
  }
  
//  public List<String> getOutput() {
//    return m_output;
//  }
  
  @Override
  public String toString() {
    List<String> output = Reporter.getOutput(this);
    String result = "[TestResult: " + getName() 
        + " STATUS:" + m_status
        + " METHOD:" + m_method;
    result += output != null && output.size() > 0 ? output.get(0) : ""
        + "]";  
        
      return result;
  }

  public String getHost() {
    return m_host;
  }
  
  public void setHost(String host) {
    m_host = host;
  }
  
  public Object[] getParameters() {
    return m_parameters;
  }

  public void setParameters(Object[] parameters) {
    m_parameters = parameters;
  }
 
}
