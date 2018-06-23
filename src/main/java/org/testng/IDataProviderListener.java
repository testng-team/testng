package org.testng;

/** A listener that gets invoked before and after a data provider is invoked by TestNG. */
public interface IDataProviderListener extends ITestNGListener {

  /**
   * This method gets invoked just before a data provider is invoked.
   *
   * @param dataProviderMethod - A {@link IDataProviderMethod} object that contains details about
   *     the data provider that is about to be executed.
   * @param method - The {@link ITestNGMethod} method that is going to consume the data
   * @param iTestContext - The current test context
   */
  default void beforeDataProviderExecution(
      IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
    // not implemented
  }

  /**
   * This method gets invoked just after a data provider is invoked.
   *
   * @param dataProviderMethod - A {@link IDataProviderMethod} object that contains details about
   *     the data provider that got executed.
   * @param method - The {@link ITestNGMethod} method that received the data
   * @param iTestContext - The current test context
   */
  default void afterDataProviderExecution(
      IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
    // not implemented
  }
}
