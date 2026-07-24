package test.listeners.github2385;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class TestClassListener implements IInvokedMethodListener {
  public static boolean listenerExecuted;
  public static boolean listenerMethodInvoked;

  public TestClassListener() {
    listenerExecuted = true;
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    listenerMethodInvoked = true;
  }
}
