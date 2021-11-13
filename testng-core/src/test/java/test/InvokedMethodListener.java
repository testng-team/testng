package test;

import java.util.ArrayList;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class InvokedMethodListener implements IInvokedMethodListener {

  private final List<String> invokedMethods = new ArrayList<>();

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    invokedMethods.add(method.getTestMethod().getMethodName());
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {}

  public List<String> getInvokedMethods() {
    return invokedMethods;
  }
}
