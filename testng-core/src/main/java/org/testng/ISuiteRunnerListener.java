package org.testng;

public interface ISuiteRunnerListener {

  ITestListener getExitCodeListener();

  void beforeInvocation(IInvokedMethod method, ITestResult testResult);

  void afterInvocation(IInvokedMethod method, ITestResult testResult);
}
