package org.testng.annotations;

import org.testng.IRetryAnalyzer;
import org.testng.internal.annotations.IDataProvidable;

/**
 * Encapsulate the &#64;Test / &#64;testng.test annotation.
 *
 * Created on Dec 20, 2005
 * @author <a href = "mailto:cedric&#64;beust.com">Cedric Beust</a>
 */
public interface ITestAnnotation extends ITestOrConfiguration, IDataProvidable {
  /**
   * Returns the number of times this method should be invoked.
   * @return the number of times this method should be invoked.
   */
  public int getInvocationCount();
  public void setInvocationCount(int l);

  /**
   * The size of the thread pool for this method.  The method will be invoked
   * from multiple threads as specified by invocationCount.
   * Note:  this attribute is ignored if invocationCount is not specified
   */
  public int getThreadPoolSize();
  public void setThreadPoolSize(int n);

  /**
   * The percentage of success expected from this method.
   */
  public int getSuccessPercentage();
  public void setSuccessPercentage(int s);

  /**
   * If set to true, this test method will always be run even if it depends
   * on a method that failed.  This attribute will be ignored if this test
   * doesn't depend on any method or group.
   */
  public boolean getAlwaysRun();
  public void setAlwaysRun(boolean f);

  public Class<?>[] getExpectedExceptions();
  public void setExpectedExceptions(Class<?>[] e);

  public String getExpectedExceptionsMessageRegExp();
  public void setExpectedExceptionsMessageRegExp(String e);

  public String getSuiteName();
  public void setSuiteName(String s);

  public String getTestName();
  public void setTestName(String s);

  public boolean getSequential();
  public void setSequential(boolean f);

  public boolean getSingleThreaded();
  public void setSingleThreaded(boolean f);

  public String getDataProvider();
  public void setDataProvider(String v);

  public Class<?> getDataProviderClass();
  public void setDataProviderClass(Class<?> v);

  public IRetryAnalyzer getRetryAnalyzer();
  public void setRetryAnalyzer(Class<?> c);

  public boolean skipFailedInvocations();
  public void setSkipFailedInvocations(boolean skip);

  public long invocationTimeOut();
  public void setInvocationTimeOut(long timeOut);

  public boolean ignoreMissingDependencies();
  public void setIgnoreMissingDependencies(boolean ignore);

  /**
   * The scheduling priority. Lower priorities get scheduled first.
   */
  public int getPriority();
  public void setPriority(int priority);
}
