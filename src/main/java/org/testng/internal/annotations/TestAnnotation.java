package org.testng.internal.annotations;

import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.ClassHelper;

/**
 * An implementation of ITest
 */
public class TestAnnotation extends TestOrConfiguration implements ITestAnnotation {

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
  private boolean m_singleThreaded = false;
  private Class<?> m_dataProviderClass = null;
  private IRetryAnalyzer m_retryAnalyzer = null;
  private Class<? extends IRetryAnalyzer> m_retryAnalyzerClass = null;
  private boolean m_skipFailedInvocations = false;
  private boolean m_ignoreMissingDependencies = false;

  /** @return the expectedExceptions */
  @Override
  public Class<?>[] getExpectedExceptions() {
    return m_expectedExceptions;
  }

  /** @param expectedExceptions the expectedExceptions to set */
  @Override
  public void setExpectedExceptions(Class<?>[] expectedExceptions) {
    m_expectedExceptions = expectedExceptions;
  }

  @Override
  public String getExpectedExceptionsMessageRegExp() {
    return m_expectedExceptionsMessageRegExp;
  }

  @Override
  public void setExpectedExceptionsMessageRegExp(String expectedExceptionsMessageRegExp) {
    m_expectedExceptionsMessageRegExp = expectedExceptionsMessageRegExp;
  }

  @Override
  public void setAlwaysRun(boolean alwaysRun) {
    m_alwaysRun = alwaysRun;
  }

  @Override
  public void setDataProvider(String dataProvider) {
    m_dataProvider = dataProvider;
  }

  @Override
  public Class<?> getDataProviderClass() {
    return m_dataProviderClass;
  }

  @Override
  public void setDataProviderClass(Class<?> dataProviderClass) {
    m_dataProviderClass = dataProviderClass;
  }

  @Override
  public void setInvocationCount(int invocationCount) {
    m_invocationCount = invocationCount;
  }

  @Override
  public void setSuccessPercentage(int successPercentage) {
    m_successPercentage = successPercentage;
  }

  @Override
  public int getInvocationCount() {
    return m_invocationCount;
  }

  @Override
  public long invocationTimeOut() {
    return m_invocationTimeOut;
  }

  @Override
  public void setInvocationTimeOut(long timeOut) {
    m_invocationTimeOut = timeOut;
  }

  @Override
  public int getSuccessPercentage() {
    return m_successPercentage;
  }

  @Override
  public String getDataProvider() {
    return m_dataProvider;
  }

  @Override
  public boolean getAlwaysRun() {
    return m_alwaysRun;
  }

  @Override
  public int getThreadPoolSize() {
    return m_threadPoolSize;
  }

  @Override
  public void setThreadPoolSize(int threadPoolSize) {
    m_threadPoolSize = threadPoolSize;
  }

  @Override
  public String getSuiteName() {
    return m_suiteName;
  }

  @Override
  public void setSuiteName(String xmlSuite) {
    m_suiteName = xmlSuite;
  }

  @Override
  public String getTestName() {
    return m_testName;
  }

  @Override
  public void setTestName(String xmlTest) {
    m_testName = xmlTest;
  }

  @Override
  public boolean getSingleThreaded() {
    return m_singleThreaded;
  }

  @Override
  public void setSingleThreaded(boolean singleThreaded) {
    m_singleThreaded = singleThreaded;
  }

  @Override
  public IRetryAnalyzer getRetryAnalyzer() {
    return m_retryAnalyzer;
  }

  @Override
  public void setRetryAnalyzer(Class<? extends IRetryAnalyzer> c) {
    if (isRetryAnalyzerNotTestNGInjected(c)) {
      m_retryAnalyzer = ClassHelper.newInstance(c);
    }
    m_retryAnalyzerClass = c;
  }

  @Override
  public Class<? extends IRetryAnalyzer> getRetryAnalyzerClass() {
    return m_retryAnalyzerClass;
  }

  @Override
  public void setSkipFailedInvocations(boolean skip) {
    m_skipFailedInvocations = skip;
  }

  @Override
  public boolean skipFailedInvocations() {
    return m_skipFailedInvocations;
  }

  @Override
  public void setIgnoreMissingDependencies(boolean ignore) {
    m_ignoreMissingDependencies = ignore;
  }

  @Override
  public boolean ignoreMissingDependencies() {
    return m_ignoreMissingDependencies;
  }

  private static boolean isRetryAnalyzerNotTestNGInjected(Class<? extends IRetryAnalyzer> c) {
    return !DisabledRetryAnalyzer.class.equals(c);
  }
}
