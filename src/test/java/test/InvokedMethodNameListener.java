package test;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InvokedMethodNameListener implements IInvokedMethodListener {

  private final List<String> invokedMethodNames = new ArrayList<>();

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    invokedMethodNames.add(method.getTestMethod().getConstructorOrMethod().getName());
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) { }

  public List<String> getInvokedMethodNames() {
    return Collections.unmodifiableList(invokedMethodNames);
  }
}
