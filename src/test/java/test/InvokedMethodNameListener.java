package test;

import com.google.common.base.Joiner;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO replace other test IInvokedMethodListener by this one
public class InvokedMethodNameListener implements IInvokedMethodListener, ITestListener {

  private final List<String> foundMethodNames = new ArrayList<>();
  private final List<String> invokedMethodNames = new ArrayList<>();
  private final List<String> failedMethodNames = new ArrayList<>();
  private final List<String> failedBeforeInvocationMethodNames = new ArrayList<>();
  private final List<String> skippedMethodNames = new ArrayList<>();
  private final List<String> skippedBeforeInvocationMethodNames = new ArrayList<>();
  private final List<String> succeedMethodNames = new ArrayList<>();
  private final Map<String, ITestResult> results = new HashMap<>();

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    invokedMethodNames.add(getName(testResult));
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    String name = getName(testResult);
    switch (testResult.getStatus()) {
      case ITestResult.FAILURE:
        failedMethodNames.add(name);
        break;
      case ITestResult.SKIP:
        skippedMethodNames.add(name);
        break;
      case ITestResult.SUCCESS:
        succeedMethodNames.add(name);
        break;
      default:
        throw new AssertionError("Unexpected value: " + testResult.getStatus());
    }
  }

  @Override
  public void onTestStart(ITestResult result) {
    foundMethodNames.add(getName(result));
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    String name = getName(result);
    results.put(name, result);
    if (!succeedMethodNames.contains(name)) {
      throw new IllegalStateException("A succeed test is supposed to be invoked");
    }
  }

  @Override
  public void onTestFailure(ITestResult result) {
    String name = getName(result);
    results.put(name, result);
    if (!failedMethodNames.contains(name)) {
      failedBeforeInvocationMethodNames.add(name);
    }
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    String name = getName(result);
    results.put(name, result);
    if (!skippedMethodNames.contains(name)) {
      skippedBeforeInvocationMethodNames.add(name);
    }
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    String name = getName(result);
    results.put(name, result);
    if (!succeedMethodNames.contains(name) || !failedMethodNames.contains(name)) {
      throw new IllegalStateException("A FailedButWithinSuccessPercentage test is supposed to be invoked");
    }
  }

  @Override
  public void onStart(ITestContext context) {
  }

  @Override
  public void onFinish(ITestContext context) {
  }

  private static String getName(ITestResult result) {
    String name = result.getMethod().getConstructorOrMethod().getName();
    if (result.getParameters().length != 0) {
      name = name + "(" + Joiner.on(",").useForNull("null").join(result.getParameters()) + ")";
    }
    return name;
  }

  public List<String> getInvokedMethodNames() {
    return Collections.unmodifiableList(invokedMethodNames);
  }

  public List<String> getFailedMethodNames() {
    return Collections.unmodifiableList(failedMethodNames);
  }

  public List<String> getSkippedMethodNames() {
    return Collections.unmodifiableList(skippedMethodNames);
  }

  public List<String> getSucceedMethodNames() {
    return Collections.unmodifiableList(succeedMethodNames);
  }

  public List<String> getFailedBeforeInvocationMethodNames() {
    return Collections.unmodifiableList(failedBeforeInvocationMethodNames);
  }

  public List<String> getSkippedBeforeInvocationMethodNames() {
    return Collections.unmodifiableList(skippedBeforeInvocationMethodNames);
  }

  public ITestResult getResult(String name) {
    return results.get(name);
  }
}
