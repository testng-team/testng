package test_result;

import java.util.Arrays;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;

public class OrderAbidingListener implements IInvokedMethodListener, ITestListener {

  private final List<ITestNGListener> listeners;

  public OrderAbidingListener(ITestNGListener... listeners) {
    this.listeners = Arrays.asList(listeners);
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    listeners.stream()
        .filter(l -> l instanceof IInvokedMethodListener)
        .map(l -> (IInvokedMethodListener) l)
        .forEach(l -> l.beforeInvocation(method, testResult));
  }

  @Override
  public void beforeInvocation(
      IInvokedMethod method, ITestResult testResult, ITestContext context) {
    listeners.stream()
        .filter(l -> l instanceof IInvokedMethodListener)
        .map(l -> (IInvokedMethodListener) l)
        .forEach(l -> l.beforeInvocation(method, testResult, context));
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    listeners.stream()
        .filter(l -> l instanceof IInvokedMethodListener)
        .map(l -> (IInvokedMethodListener) l)
        .forEach(l -> l.afterInvocation(method, testResult));
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {
    listeners.stream()
        .filter(l -> l instanceof IInvokedMethodListener)
        .map(l -> (IInvokedMethodListener) l)
        .forEach(l -> l.afterInvocation(method, testResult, context));
  }

  @Override
  public void onStart(ITestContext context) {
    listeners.stream()
        .filter(l -> l instanceof ITestListener)
        .map(l -> (ITestListener) l)
        .forEach(l -> l.onStart(context));
  }

  @Override
  public void onTestStart(ITestResult result) {
    listeners.stream()
        .filter(l -> l instanceof ITestListener)
        .map(l -> (ITestListener) l)
        .forEach(l -> l.onTestStart(result));
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    listeners.stream()
        .filter(l -> l instanceof ITestListener)
        .map(l -> (ITestListener) l)
        .forEach(l -> l.onTestSuccess(result));
  }

  @Override
  public void onTestFailure(ITestResult result) {
    listeners.stream()
        .filter(l -> l instanceof ITestListener)
        .map(l -> (ITestListener) l)
        .forEach(l -> l.onTestFailure(result));
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    listeners.stream()
        .filter(l -> l instanceof ITestListener)
        .map(l -> (ITestListener) l)
        .forEach(l -> l.onTestSkipped(result));
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    listeners.stream()
        .filter(l -> l instanceof ITestListener)
        .map(l -> (ITestListener) l)
        .forEach(l -> l.onTestFailedButWithinSuccessPercentage(result));
  }

  @Override
  public void onTestFailedWithTimeout(ITestResult result) {
    listeners.stream()
        .filter(l -> l instanceof ITestListener)
        .map(l -> (ITestListener) l)
        .forEach(l -> l.onTestFailedWithTimeout(result));
  }

  @Override
  public void onFinish(ITestContext context) {
    listeners.stream()
        .filter(l -> l instanceof ITestListener)
        .map(l -> (ITestListener) l)
        .forEach(l -> l.onFinish(context));
  }
}
