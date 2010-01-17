package org.testng.internal.annotations;

import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;


/**
 * An implementation of ITest
 * 
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class TestAnnotation extends TestOrConfiguration implements ITestAnnotation {
  private long m_timeOut = 0;
  private long m_invocationTimeOut = 0;
  private int m_invocationCount = 1;
  private int m_threadPoolSize = 0;
  private int m_successPercentage = 100;
  private String m_dataProvider = "";
  private boolean m_alwaysRun = false;
  private Class<?>[] m_expectedExceptions = {};
  private String m_expectedExceptionsMessageRegExp = ".*";
  private String m_suiteName = "";
  private String m_testName = "";
  private boolean m_sequential = false;
  private Class<?> m_dataProviderClass = null;
  private IRetryAnalyzer m_retryAnalyzer = null;
  private boolean m_skipFailedInvocations = false;
  private boolean m_ignoreMissingDependencies = false;
  
  /**
   * @return the expectedExceptions
   */
  public Class<?>[] getExpectedExceptions() {
    return m_expectedExceptions;
  }

  /**
   * @param expectedExceptions the expectedExceptions to set
   */
  public void setExpectedExceptions(Class<?>[] expectedExceptions) {
    m_expectedExceptions = expectedExceptions;
  }

  public String getExpectedExceptionsMessageRegExp() {
    return m_expectedExceptionsMessageRegExp;
  }

  public void setExpectedExceptionsMessageRegExp(
      String expectedExceptionsMessageRegExp) {
    m_expectedExceptionsMessageRegExp = expectedExceptionsMessageRegExp;
  }

  public void setAlwaysRun(boolean alwaysRun) {
    m_alwaysRun = alwaysRun;
  }

  public void setDataProvider(String dataProvider) {
    m_dataProvider = dataProvider;
  }

  public Class<?> getDataProviderClass() {
    return m_dataProviderClass;
  }

  public void setDataProviderClass(Class<?> dataProviderClass) {
    m_dataProviderClass = dataProviderClass;
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
  
  public long getTimeOut() {
    return m_timeOut;
  }

  public int getInvocationCount() {
    return m_invocationCount;
  }
  
  public long invocationTimeOut() {
   return m_invocationTimeOut; 
  }

  public void setInvocationTimeOut(long timeOut) {
    m_invocationTimeOut = timeOut;
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

  public IRetryAnalyzer getRetryAnalyzer() {
    return m_retryAnalyzer;
  }

  public void setRetryAnalyzer(Class<?> c) {
    m_retryAnalyzer = null;

    if (c != null && IRetryAnalyzer.class.isAssignableFrom(c)) {
      try {
        m_retryAnalyzer = (IRetryAnalyzer) c.newInstance();
      } 
      catch (InstantiationException e) {
        // The class will never be called.
      } 
      catch (IllegalAccessException e) {
        // The class will never be called.
      }
    }
  }

  public void setSkipFailedInvocations(boolean skip) {
    m_skipFailedInvocations = skip;
  }

  public boolean skipFailedInvocations() {
    return m_skipFailedInvocations;
  }
  
  public void setIgnoreMissingDependencies(boolean ignore) {
    m_ignoreMissingDependencies = ignore;
  }

  public boolean ignoreMissingDependencies() {
    return m_ignoreMissingDependencies;
  }
}
