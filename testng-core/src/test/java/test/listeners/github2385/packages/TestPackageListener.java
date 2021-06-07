package test.listeners.github2385.packages;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class TestPackageListener implements IInvokedMethodListener {
  public static boolean listenerExecuted = false;
  public static boolean listenerMethodInvoked = false;

  public TestPackageListener() {
    listenerExecuted = true;
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    listenerMethodInvoked = true;
  }
}
