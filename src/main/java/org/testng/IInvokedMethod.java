package org.testng;

/**
 * An interface representing a method that has been invoked by TestNG.
 *
 * This interface is internal.
 */
public interface IInvokedMethod {

  /**
   * @return true if this method is a test method
   */
  public abstract boolean isTestMethod();

  /**
   * @return true if this method is a configuration method (@BeforeXXX or @AfterXXX)
   */
  public abstract boolean isConfigurationMethod();

  /**
   * @return the test method
   */
  public abstract ITestNGMethod getTestMethod();

  public ITestResult getTestResult();

  /**
   * @return the date when this method was run
   */
  public abstract long getDate();

}