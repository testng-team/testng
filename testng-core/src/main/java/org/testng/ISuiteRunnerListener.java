package org.testng;

import org.jspecify.annotations.Nullable;

public interface ISuiteRunnerListener {

  @Nullable
  ITestListener getExitCodeListener();

  void beforeInvocation(IInvokedMethod method, ITestResult testResult);

  void afterInvocation(IInvokedMethod method, ITestResult testResult);
}
