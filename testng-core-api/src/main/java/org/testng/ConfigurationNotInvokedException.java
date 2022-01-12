package org.testng;

/**
 * Represents an exception that is thrown when a configuration method is not invoked. One of the
 * use-cases when this can happen is when the user does the following:
 *
 * <ul>
 *   <li>User defines a configuration method
 *   <li>The class that houses the configuration method defines support for callbacks via {@link
 *       IConfigurable} implementation
 *   <li>User willfully skips invoking the callback and also fails at altering the configuration
 *       method's status via {@link ITestResult#setStatus(int)}
 * </ul>
 */
public class ConfigurationNotInvokedException extends TestNGException {

  public ConfigurationNotInvokedException(ITestNGMethod tm) {
    super(
        tm.getQualifiedName()
            + " defines a callback via "
            + IConfigurable.class.getName()
            + " but neither the callback was invoked nor the status was altered to "
            + String.join("|", ITestResult.finalStatuses()));
  }
}
