package test.listeners.github2522;

import java.util.HashMap;
import java.util.Map;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class TestListener implements IInvokedMethodListener {
  private boolean hasFailures = false;
  public static Map<String, Integer> beforeInvocation = new HashMap<>();
  public static Map<String, Integer> afterInvocation = new HashMap<>();

  @Override
  public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult result) {
    beforeInvocation.put(result.getTestClass().getName() + result.getName(), result.getStatus());
    if (hasFailures) {
      result.setStatus(ITestResult.SKIP);
    }
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    afterInvocation.put(
        testResult.getTestClass().getName() + testResult.getName(), testResult.getStatus());
    if (!method.isTestMethod()) {
      return;
    }
    if (!testResult.isSuccess()) {
      hasFailures = true;
    }
  }
}
