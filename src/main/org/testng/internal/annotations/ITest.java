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
  public long getTimeOut();
  public void setTimeOut(long l);
  
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
  
  /**
   * The percentage of success expected from this method.
   */
  public int getSuccessPercentage();
  
  /**
   * The name of the data provider for this test method.
   * @see org.testng.annotations.DataProvider#DataProvider
   */
  public String getDataProvider();
  
  /**
   * If set to true, this test method will always be run even if it depends
   * on a method that failed.  This attribute will be ignored if this test
   * doesn't depend on any method or group.
   */
  public boolean getAlwaysRun();

  public String[] getGroups();

  public boolean getEnabled();

  public void setDependsOnGroups(String[] dependsOnGroups);

  public void setDependsOnMethods(String[] dependsOnMethods);

  public void setEnabled(boolean enabled);

  public void setGroups(String[] groups);

  public String[] getDependsOnGroups();

  public String[] getDependsOnMethods();

  public String getDescription();
 
  public Class[] getExpectedExceptions();
  
  public String getSuiteName();
  
  public String getTestName();
  
  public boolean getSequential();
}
