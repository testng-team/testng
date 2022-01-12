package org.testng;

/**
 * Represents an exception that is thrown when a test method is not invoked. One of the use-cases
 * when this can happen is when the user does the following:
 *
 * <ul>
 *   <li>User defines a test method
 *   <li>The class that houses the test method defines support for callbacks via {@link IHookable}
 *       implementation
 *   <li>User willfully skips invoking the callback and also fails at altering the test method's
 *       status via {@link ITestResult#setStatus(int)}
 * </ul>
 */
public class TestNotInvokedException extends TestNGException {
  public TestNotInvokedException(ITestNGMethod tm) {
    super(
        tm.getQualifiedName()
            + " defines a callback via "
            + IHookable.class.getName()
            + " but neither the callback was invoked nor the status was altered to "
            + String.join("|", ITestResult.finalStatuses()));
  }
}
