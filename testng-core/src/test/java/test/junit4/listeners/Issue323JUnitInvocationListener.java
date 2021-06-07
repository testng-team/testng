package test.junit4.listeners;

import java.util.ArrayList;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class Issue323JUnitInvocationListener implements IInvokedMethodListener {
  public static List<String> messages = new ArrayList<>();

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    messages.add("beforeInvocation");
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    messages.add("afterInvocation");
  }
}
