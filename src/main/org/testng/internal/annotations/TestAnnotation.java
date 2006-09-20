package org.testng.internal.annotations;

import org.testng.ITestResult;
import org.testng.TestNG;


/**
 * An implementation of ITest
 * 
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class TestAnnotation extends TestOrConfiguration implements ITest {
  private long m_timeOut = 0;
  private int m_invocationCount = 1;
  private int m_threadPoolSize = 0;
  private int m_successPercentage = 100;
  private String m_dataProvider = "";
  private boolean m_alwaysRun = false;
  private Class[] m_expectedExceptions = {};
  private String m_suiteName = TestNG.DEFAULT_SUITE_NAME;
  private String m_testName = TestNG.DEFAULT_TEST_NAME;
  private boolean m_sequential = false;
  private IAnnotationTransformer m_transformer;
  private boolean m_reentering = false;
  
  public TestAnnotation(IAnnotationTransformer transformer) {
    m_transformer = transformer;
  }

  /**
   * @return the expectedExceptions
   */
  public Class[] getExpectedExceptions() {
    return m_expectedExceptions;
  }

  /**
   * @param expectedExceptions the expectedExceptions to set
   */
  public void setExpectedExceptions(Class[] expectedExceptions) {
    m_expectedExceptions = expectedExceptions;
  }

  public void setAlwaysRun(boolean alwaysRun) {
    m_alwaysRun = alwaysRun;
  }

  public void setDataProvider(String dataProvider) {
    m_dataProvider = dataProvider;
  }

  public void setInvocationCount(int invocationCount) {
    m_invocationCount = invocationCount;
  }

  public void setSuccessPercentage(int successPercentage) {
    m_successPercentage = successPercentage;
  }

  public void setTimeOut(long timeOut) {
    m_timeOut = timeOut;
  }
  
  private ITest transform(ITest test) {
    ITest result = m_transformer.transform(test, null);
    result.setMethod(test.getMethod());
    result.setTestClass(test.getTestClass());
    result.setConstructor(test.getConstructor());
    
    return result;
  }

  public long getTimeOut() {
    long result = m_timeOut;

    if (! m_reentering) {
      m_reentering  = true;
      ITest tTest = transform(this);
      result = tTest.getTimeOut();
      m_reentering = false;
    }
    
    return result;
  }

  public int getInvocationCount() {
    int result = m_invocationCount;

    if (! m_reentering) {
      m_reentering  = true;
      ITest tTest = transform(this);
      result = tTest.getInvocationCount();
      m_reentering = false;
    }
    
    return result;
  }

  public int getSuccessPercentage() {
    return m_successPercentage;
  }

  public String getDataProvider() {
    return m_dataProvider;
  }

  public boolean getAlwaysRun() {
    return m_alwaysRun;
  }

  public int getThreadPoolSize() {
    return m_threadPoolSize;
  }

  public void setThreadPoolSize(int threadPoolSize) {
    m_threadPoolSize = threadPoolSize;
  }

  public String getSuiteName() {
    return m_suiteName;
  }

  public void setSuiteName(String xmlSuite) {
    m_suiteName = xmlSuite;
  }

  public String getTestName() {
    return m_testName;
  }

  public void setTestName(String xmlTest) {
    m_testName = xmlTest;
  }
  
  public boolean getSequential() {
    return m_sequential;
  }
  
  public void setSequential(boolean sequential) {
    m_sequential = sequential;
  }
  
}
