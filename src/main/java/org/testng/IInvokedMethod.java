package org.testng;

/**
 * An interface representing a method that has been invoked by TestNG.
 *
 * <p>This interface is internal.
 */
public interface IInvokedMethod {

  /** @return true if this method is a test method */
  boolean isTestMethod();

  /** @return true if this method is a configuration method (@BeforeXXX or @AfterXXX) */
  boolean isConfigurationMethod();

  /** @return the test method */
  ITestNGMethod getTestMethod();

  ITestResult getTestResult();

  /** @return the date when this method was run */
  long getDate();
}
