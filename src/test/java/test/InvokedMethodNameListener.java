package test;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO replace other test IInvokedMethodListener by this one
public class InvokedMethodNameListener implements IInvokedMethodListener {

  private final List<String> invokedMethodNames = new ArrayList<>();
  private final List<String> failedMethodNames = new ArrayList<>();
  private final List<String> skippedMethodNames = new ArrayList<>();
  private final List<String> succeedMethodNames = new ArrayList<>();

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    invokedMethodNames.add(method.getTestMethod().getConstructorOrMethod().getName());
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    switch (testResult.getStatus()) {
      case ITestResult.FAILURE:
        failedMethodNames.add(method.getTestMethod().getConstructorOrMethod().getName());
        break;
      case ITestResult.SKIP:
        skippedMethodNames.add(method.getTestMethod().getConstructorOrMethod().getName());
        break;
      case ITestResult.SUCCESS:
        succeedMethodNames.add(method.getTestMethod().getConstructorOrMethod().getName());
        break;
    }
  }

  public List<String> getInvokedMethodNames() {
    return Collections.unmodifiableList(invokedMethodNames);
  }

  public List<String> getFailedMethodNames() {
    return failedMethodNames;
  }

  public List<String> getSkippedMethodNames() {
    return skippedMethodNames;
  }

  public List<String> getSucceedMethodNames() {
    return succeedMethodNames;
  }
}
