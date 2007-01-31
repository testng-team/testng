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

  public Class[] getExpectedExceptions();
  public void setExpectedExceptions(Class[] e);
  
  public String getSuiteName();
  public void setSuiteName(String s);
  
  public String getTestName();
  public void setTestName(String s);
  
  public boolean getSequential();
  public void setSequential(boolean f);
  
  public String getDataProvider();
  public Class getDataProviderClass();
}
