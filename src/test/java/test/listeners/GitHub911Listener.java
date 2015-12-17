package test.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class GitHub911Listener implements ITestListener {

  int onTestStart = 0;
  int onTestSuccess = 0;
  int onTestFailure = 0;
  int onTestSkipped = 0;
  int onTestFailedButWithinSuccessPercentage = 0;
  int onStart = 0;
  int onFinish = 0;

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
