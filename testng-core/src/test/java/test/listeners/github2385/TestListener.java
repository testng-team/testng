package test.listeners.github2385;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public final class TestListener implements IInvokedMethodListener {
  public static boolean listenerExecuted = false;
  public static boolean listenerMethodInvoked = false;

  public TestListener() {
    listenerExecuted = true;
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    listenerMethodInvoked = true;
  }
}
