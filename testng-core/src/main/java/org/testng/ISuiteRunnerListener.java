package org.testng;

/**
 * Lets the {@link TestRunner} of each {@code <test>} call back into the runner of its suite.
 *
 * <p>{@link SuiteRunner} implements this interface.
 */
public interface ISuiteRunnerListener {

  /**
   * Returns the listener that TestNG uses to work out the exit code of the run.
   *
   * @return the exit code listener.
   */
  ITestListener getExitCodeListener();

  /**
   * TestNG calls this method before it calls the {@link IInvokedMethodListener} listeners of a
   * method. It calls it only when there is at least one such listener.
   *
   * @param method the method that is about to run.
   * @param testResult the result of the method.
   */
  void beforeInvocation(IInvokedMethod method, ITestResult testResult);

  /**
   * TestNG calls this method after it calls the {@link IInvokedMethodListener} listeners of a
   * method. It calls it only when there is at least one such listener.
   *
   * @param method the method that ran.
   * @param testResult the result of the method.
   */
  void afterInvocation(IInvokedMethod method, ITestResult testResult);
}
