package test.listeners;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class LListener implements IInvokedMethodListener {
  public static boolean invoked = false;

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    invoked = true;
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
  }

}
