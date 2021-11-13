package org.testng.annotations;

import org.testng.IRetryAnalyzer;
import org.testng.internal.annotations.IDataProvidable;

/** Encapsulate the &#64;Test / &#64;testng.test annotation. */
public interface ITestAnnotation extends ITestOrConfiguration, IDataProvidable {
  /**
   * Returns the number of times this method should be invoked.
   *
   * @return the number of times this method should be invoked.
   */
  int getInvocationCount();

  void setInvocationCount(int l);

  /**
   * The size of the thread pool for this method. The method will be invoked from multiple threads
   * as specified by invocationCount. Note: this attribute is ignored if invocationCount is not
   * specified
   *
   * @return the value
   */
  int getThreadPoolSize();

  void setThreadPoolSize(int n);

  /**
   * The percentage of success expected from this method.
   *
   * @return the value
   */
  int getSuccessPercentage();

  void setSuccessPercentage(int s);

  /**
   * If set to true, this test method will always be run even if it depends on a method that failed.
   * This attribute will be ignored if this test doesn't depend on any method or group.
   *
   * @return the value
   */
  boolean getAlwaysRun();

  void setAlwaysRun(boolean f);

  Class<?>[] getExpectedExceptions();

  void setExpectedExceptions(Class<?>[] e);

  String getExpectedExceptionsMessageRegExp();

  void setExpectedExceptionsMessageRegExp(String e);

  String getSuiteName();

  void setSuiteName(String s);

  String getTestName();

  void setTestName(String s);

  boolean getSingleThreaded();

  void setSingleThreaded(boolean f);

  String getDataProvider();

  void setDataProvider(String v);

  Class<?> getDataProviderClass();

  void setDataProviderClass(Class<?> v);

  void setRetryAnalyzer(Class<? extends IRetryAnalyzer> c);

  Class<? extends IRetryAnalyzer> getRetryAnalyzerClass();

  boolean skipFailedInvocations();

  void setSkipFailedInvocations(boolean skip);

  long invocationTimeOut();

  void setInvocationTimeOut(long timeOut);

  boolean ignoreMissingDependencies();

  void setIgnoreMissingDependencies(boolean ignore);

  /**
   * The scheduling priority. Lower priorities get scheduled first.
   *
   * @return the value
   */
  int getPriority();

  void setPriority(int priority);

  CustomAttribute[] getAttributes();

  void setAttributes(CustomAttribute[] attributes);
}
