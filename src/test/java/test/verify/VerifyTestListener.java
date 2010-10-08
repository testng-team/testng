package test.verify;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class VerifyTestListener implements ITestListener {
  public static int m_count = 0;

  @Override
  public void onFinish(ITestContext context) {
  }

  @Override
  public void onStart(ITestContext context) {
    m_count++;
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
  }

  @Override
  public void onTestFailure(ITestResult result) {
  }

  @Override
  public void onTestSkipped(ITestResult result) {
  }

  @Override
  public void onTestStart(ITestResult result) {
  }

  @Override
  public void onTestSuccess(ITestResult result) {
  }

}
