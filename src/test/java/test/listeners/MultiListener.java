package test.listeners;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.IResultListener;

public class MultiListener implements ISuiteListener, IResultListener, IInvokedMethodListener {

  private int onConfigurationSuccessCount = 0;
  private int onConfigurationFailureCount = 0;
  private int onConfigurationSkipCount = 0;
  private int beforeInvocationCount = 0;
  private int afterInvocationCount = 0;
  private int onSuiteStartCount = 0;
  private int onSuiteFinishCount = 0;
  private int onMethodTestStartCount = 0;
  private int onMethodTestSuccessCount = 0;
  private int onMethodTestFailureCount = 0;
  private int onMethodTestSkippedCount = 0;
  private int onMethodTestFailedButWithinSuccessPercentageCount = 0;
  private int onTestStartCount = 0;
  private int onTestFinishCount = 0;

  @Override
  public void onConfigurationSuccess(ITestResult itr) {
    onConfigurationSuccessCount++;
  }

  @Override
  public void onConfigurationFailure(ITestResult itr) {
    onConfigurationFailureCount++;
  }

  @Override
  public void onConfigurationSkip(ITestResult itr) {
    onConfigurationSkipCount++;
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    beforeInvocationCount++;
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    afterInvocationCount++;
  }

  @Override
  public void onStart(ISuite suite) {
    onSuiteStartCount++;
  }

  @Override
  public void onFinish(ISuite suite) {
    onSuiteFinishCount++;
  }

  @Override
  public void onTestStart(ITestResult result) {
    onMethodTestStartCount++;
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    onMethodTestSuccessCount++;
  }

  @Override
  public void onTestFailure(ITestResult result) {
    onMethodTestFailureCount++;
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    onMethodTestSkippedCount++;
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    onMethodTestFailedButWithinSuccessPercentageCount++;
  }

  @Override
  public void onStart(ITestContext context) {
    onTestStartCount++;
  }

  @Override
  public void onFinish(ITestContext context) {
    onTestFinishCount++;
  }

  public int getOnConfigurationSuccessCount() {
    return onConfigurationSuccessCount;
  }

  public int getOnConfigurationFailureCount() {
    return onConfigurationFailureCount;
  }

  public int getOnConfigurationSkipCount() {
    return onConfigurationSkipCount;
  }

  public int getBeforeInvocationCount() {
    return beforeInvocationCount;
  }

  public int getAfterInvocationCount() {
    return afterInvocationCount;
  }

  public int getOnSuiteStartCount() {
    return onSuiteStartCount;
  }

  public int getOnSuiteFinishCount() {
    return onSuiteFinishCount;
  }

  public int getOnMethodTestStartCount() {
    return onMethodTestStartCount;
  }

  public int getOnMethodTestSuccessCount() {
    return onMethodTestSuccessCount;
  }

  public int getOnMethodTestFailureCount() {
    return onMethodTestFailureCount;
  }

  public int getOnMethodTestSkippedCount() {
    return onMethodTestSkippedCount;
  }

  public int getOnMethodTestFailedButWithinSuccessPercentageCount() {
    return onMethodTestFailedButWithinSuccessPercentageCount;
  }

  public int getOnTestStartCount() {
    return onTestStartCount;
  }

  public int getOnTestFinishCount() {
    return onTestFinishCount;
  }
}
