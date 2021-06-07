package test.listeners.github2522;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class SkipTestListener implements IInvokedMethodListener {
  @Override
  public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult result) {
    if (invokedMethod.getTestMethod().getMethodName().contains("oneTest")) {
      result.setStatus(ITestResult.SKIP);
    }
  }
}
