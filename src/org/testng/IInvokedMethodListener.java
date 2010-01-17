package org.testng;

/**
 * A listener that gets invoked before and after a method is invoked by TestNG.
 * This listener will only be invoked for configuration and test methods.
 */
public interface IInvokedMethodListener extends ITestNGListener {

  void beforeInvocation(IInvokedMethod method, ITestResult testResult);

  void afterInvocation(IInvokedMethod method, ITestResult testResult);
}
