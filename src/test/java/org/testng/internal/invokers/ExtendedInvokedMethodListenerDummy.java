package org.testng.internal.invokers;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener2;
import org.testng.ITestContext;
import org.testng.ITestResult;

class ExtendedInvokedMethodListenerDummy implements IInvokedMethodListener2 {

  public void beforeInvocation(IInvokedMethod method, ITestResult testResult,
                               ITestContext context) {
  }

  public void afterInvocation(IInvokedMethod method, ITestResult testResult,
                              ITestContext context) {
  }

  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
  }

  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
  }
}
