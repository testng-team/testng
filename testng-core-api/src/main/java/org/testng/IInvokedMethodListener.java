package org.testng;

/**
 * A listener that gets invoked before and after a method is invoked by TestNG. This listener will
 * be invoked for configuration and test methods irrespective of whether they passe/fail or get
 * skipped. This listener invocation can be disabled for SKIPPED tests through one of the below
 * mechanisms:
 *
 * <ul>
 *   <li>Command line parameter <code>alwaysRunListeners</code>
 *   <li>Build tool
 *   <li>Via {@code TestNG.alwaysRunListeners(false)}
 * </ul>
 */
public interface IInvokedMethodListener extends ITestNGListener {

  default void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    // not implemented
  }

  default void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    // not implemented
  }

  /**
   * To be implemented if the method needs a handle to contextual information.
   *
   * @param method The invoked method
   * @param testResult The test result
   * @param context The test context
   */
  default void beforeInvocation(
      IInvokedMethod method, ITestResult testResult, ITestContext context) {
    // not implemented
  }

  /**
   * To be implemented if the method needs a handle to contextual information.
   *
   * @param method The invoked method
   * @param testResult The test result
   * @param context The test context
   */
  default void afterInvocation(
      IInvokedMethod method, ITestResult testResult, ITestContext context) {
    // not implemented
  }
}
