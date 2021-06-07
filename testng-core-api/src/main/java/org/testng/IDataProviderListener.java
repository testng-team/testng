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

  /**
   * This method gets invoked when the data provider encounters an exception
   *
   * @param method - The {@link ITestNGMethod} method that received the data. A reference to the
   *     corresponding data provider can be obtained via {@link
   *     ITestNGMethod#getDataProviderMethod()}
   * @param ctx - The current test context
   * @param t - The {@link RuntimeException} that embeds the actual exception. Use {@link
   *     RuntimeException#getCause()} to get to the actual exception.
   */
  default void onDataProviderFailure(ITestNGMethod method, ITestContext ctx, RuntimeException t) {
    // not implemented
  }
}
