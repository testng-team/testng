package org.testng.internal.invokers;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

class SimpleInvokedMethodListenerDummy implements IInvokedMethodListener {

  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
  }

  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
  }
}
