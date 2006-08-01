package org.testng.internal.annotations;

/**
 * Encapsulate the &#64;Test / &#64;testng.test annotation.
 * 
 * Created on Dec 20, 2005
 * @author <a href = "mailto:cedric&#64;beust.com">Cedric Beust</a>
 */
public interface ITest extends ITestOrConfiguration {
  /**
   * Returns the maximum number of milliseconds this test should take.
   * If it hasn't returned after this time, it will be marked as a FAIL.
   * @return the maximum number of milliseconds this test should take.
   */
  long getTimeOut();
  
  /**
   * Returns the number of times this method should be invoked.
   * @return the number of times this method should be invoked.
   */
  int getInvocationCount();
  
  /**
   * The size of the thread pool for this method.  The method will be invoked
   * from multiple threads as specified by invocationCount.
   * Note:  this attribute is ignored if invocationCount is not specified
   */
  int getThreadPoolSize();
  
  /**
   * The percentage of success expected from this method.
   */
  int getSuccessPercentage();
  
  /**
   * The name of the data provider for this test method.
   * @see org.testng.annotations.DataProvider#DataProvider
   */
  String getDataProvider();
  
  /**
   * If set to true, this test method will always be run even if it depends
   * on a method that failed.  This attribute will be ignored if this test
   * doesn't depend on any method or group.
   */
  boolean getAlwaysRun();

  String[] getGroups();

  boolean getEnabled();

  void setDependsOnGroups(String[] dependsOnGroups);

  void setDependsOnMethods(String[] dependsOnMethods);

  void setEnabled(boolean enabled);

  void setGroups(String[] groups);

  String[] getDependsOnGroups();

  String[] getDependsOnMethods();

  String getDescription();
 
  public Class[] getExpectedExceptions();
  
  public String getSuiteName();
  
  public String getTestName();
}
