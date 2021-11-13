package org.testng;

import java.util.Iterator;

/**
 * This interface helps define an interceptor for data providers. Implementations of this TestNG
 * listener can be wired in via the <code>@Listeners</code> annotation or via the <code>listeners
 * </code> tag in the suite file or via a Service Provider Interface mechanism.
 *
 * <p>The implementation would be able to alter the actual set of data using which a test method
 * would be iterated upon.
 */
public interface IDataProviderInterceptor extends ITestNGListener {

  /**
   * @param original - The original data set as produced by a particular data provider.
   * @param dataProviderMethod - The {@link IDataProviderMethod} method object which represents the
   *     data provider that was invoked.
   * @param method - The {@link ITestNGMethod} method object which represents the test method that
   *     will receive the parameters.
   * @param iTestContext - The {@link ITestContext} object that represents the current test context.
   * @return - The altered data set that would be used by TestNG to run the test method.
   */
  Iterator<Object[]> intercept(
      Iterator<Object[]> original,
      IDataProviderMethod dataProviderMethod,
      ITestNGMethod method,
      ITestContext iTestContext);
}
