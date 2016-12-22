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
  private long invocationTimeOut = 0;
  private int invocationCount = 1;
  private int threadPoolSize = 0;
  private int successPercentage = 100;
  private String dataProvider = "";
  private boolean alwaysRun = false;
  private Class<?>[] expectedExceptions = {};
  private String expectedExceptionsMessageRegExp = ".*";
  private String suiteName = "";
  private String testName = "";
  private boolean singleThreaded = false;
  private boolean sequential = false;
  private Class<?> dataProviderClass = null;
  private IRetryAnalyzer retryAnalyzer = null;
  private boolean skipFailedInvocations = false;
  private boolean ignoreMissingDependencies = false;

  /**
   * @return the expectedExceptions
   */
  @Override
  public Class<?>[] getExpectedExceptions() {
    return expectedExceptions;
  }

  /**
   * @param expectedExceptions the expectedExceptions to set
   */
  @Override
  public void setExpectedExceptions(Class<?>[] expectedExceptions) {
    this.expectedExceptions = expectedExceptions;
  }

  @Override
  public String getExpectedExceptionsMessageRegExp() {
    return expectedExceptionsMessageRegExp;
  }

  @Override
  public void setExpectedExceptionsMessageRegExp(
      String expectedExceptionsMessageRegExp) {
    this.expectedExceptionsMessageRegExp = expectedExceptionsMessageRegExp;
  }

  @Override
  public void setAlwaysRun(boolean alwaysRun) {
    this.alwaysRun = alwaysRun;
  }

  @Override
  public void setDataProvider(String dataProvider) {
    this.dataProvider = dataProvider;
  }

  @Override
  public Class<?> getDataProviderClass() {
    return dataProviderClass;
  }

  @Override
  public void setDataProviderClass(Class<?> dataProviderClass) {
    this.dataProviderClass = dataProviderClass;
  }

  @Override
  public void setInvocationCount(int invocationCount) {
    this.invocationCount = invocationCount;
  }

  @Override
  public void setSuccessPercentage(int successPercentage) {
    this.successPercentage = successPercentage;
  }

  @Override
  public int getInvocationCount() {
    return invocationCount;
  }

  @Override
  public long invocationTimeOut() {
   return invocationTimeOut;
  }

  @Override
  public void setInvocationTimeOut(long timeOut) {
    invocationTimeOut = timeOut;
  }


  @Override
  public int getSuccessPercentage() {
    return successPercentage;
  }

  @Override
  public String getDataProvider() {
    return dataProvider;
  }

  @Override
  public boolean getAlwaysRun() {
    return alwaysRun;
  }

  @Override
  public int getThreadPoolSize() {
    return threadPoolSize;
  }

  @Override
  public void setThreadPoolSize(int threadPoolSize) {
    this.threadPoolSize = threadPoolSize;
  }

  @Override
  public String getSuiteName() {
    return suiteName;
  }

  @Override
  public void setSuiteName(String xmlSuite) {
    suiteName = xmlSuite;
  }

  @Override
  public String getTestName() {
    return testName;
  }

  @Override
  public void setTestName(String xmlTest) {
    testName = xmlTest;
  }

  @Override
  public boolean getSingleThreaded() {
    return singleThreaded;
  }

  @Override
  public void setSingleThreaded(boolean singleThreaded) {
    this.singleThreaded = singleThreaded;
  }

  @Override
  public boolean getSequential() {
    return sequential;
  }

  @Override
  public void setSequential(boolean sequential) {
    this.sequential = sequential;
  }

  @Override
  public IRetryAnalyzer getRetryAnalyzer() {
    return retryAnalyzer;
  }

  @Override
  public void setRetryAnalyzer(Class<?> c) {
    retryAnalyzer = null;

    if (c != null && IRetryAnalyzer.class.isAssignableFrom(c)) {
      try {
        retryAnalyzer = (IRetryAnalyzer) c.newInstance();
      }
      catch (InstantiationException | IllegalAccessException e) {
        // The class will never be called.
      }
    }
  }

  @Override
  public void setSkipFailedInvocations(boolean skip) {
    skipFailedInvocations = skip;
  }

  @Override
  public boolean skipFailedInvocations() {
    return skipFailedInvocations;
  }

  @Override
  public void setIgnoreMissingDependencies(boolean ignore) {
    ignoreMissingDependencies = ignore;
  }

  @Override
  public boolean ignoreMissingDependencies() {
    return ignoreMissingDependencies;
  }
}
