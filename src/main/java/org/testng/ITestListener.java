package org.testng;

/**
 * A listener for test running.
 *
 * @author Cedric Beust
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 * @author Hani Suleiman
 */
public interface ITestListener extends ITestNGListener {
  /**
   * Invoked each time before a test will be invoked.
   * The <code>ITestResult</code> is only partially filled with the references to
   * class, method, start millis and status.
   *
   * @param result the partially filled <code>ITestResult</code>
   * @see ITestResult#STARTED
   */
  void onTestStart(ITestResult result);

  /**
   * Invoked each time a test succeeds.
   *
   * @param result <code>ITestResult</code> containing information about the run test
   * @see ITestResult#SUCCESS
   */
  public void onTestSuccess(ITestResult result);

  /**
   * Invoked each time a test fails.
   *
   * @param result <code>ITestResult</code> containing information about the run test
   * @see ITestResult#FAILURE
   */
  public void onTestFailure(ITestResult result);

  /**
   * Invoked each time a test is skipped.
   *
   * @param result <code>ITestResult</code> containing information about the run test
   * @see ITestResult#SKIP
   */
  public void onTestSkipped(ITestResult result);

  /**
   * Invoked each time a method fails but has been annotated with
   * successPercentage and this failure still keeps it within the
   * success percentage requested.
   *
   * @param result <code>ITestResult</code> containing information about the run test
   * @see ITestResult#SUCCESS_PERCENTAGE_FAILURE
   */
  public void onTestFailedButWithinSuccessPercentage(ITestResult result);

  /**
   * Invoked after the test class is instantiated and before
   * any configuration method is called.
   */
  public void onStart(ITestContext context);

  /**
   * Invoked after all the tests have run and all their
   * Configuration methods have been called.
   */
  public void onFinish(ITestContext context);

}
