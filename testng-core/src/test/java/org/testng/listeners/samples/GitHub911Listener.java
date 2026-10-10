package org.testng.listeners.samples;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class GitHub911Listener implements ITestListener {

  public int onTestStart = 0;
  public int onTestSuccess = 0;
  public int onTestFailure = 0;
  public int onTestSkipped = 0;
  public int onTestFailedButWithinSuccessPercentage = 0;
  public int onStart = 0;
  public int onFinish = 0;

  @Override
  public void onTestStart(ITestResult result) {
    onTestStart++;
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    onTestSuccess++;
  }

  @Override
  public void onTestFailure(ITestResult result) {
    onTestFailure++;
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    onTestSkipped++;
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    onTestFailedButWithinSuccessPercentage++;
  }

  @Override
  public void onStart(ITestContext context) {
    onStart++;
  }

  @Override
  public void onFinish(ITestContext context) {
    onFinish++;
  }
}
