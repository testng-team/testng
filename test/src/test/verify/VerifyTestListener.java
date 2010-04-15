package test.verify;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class VerifyTestListener implements ITestListener {
  public static int m_count = 0;

  public void onFinish(ITestContext context) {
  }

  public void onStart(ITestContext context) {
    m_count++;
  }

  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
  }

  public void onTestFailure(ITestResult result) {
  }

  public void onTestSkipped(ITestResult result) {
  }

  public void onTestStart(ITestResult result) {
  }

  public void onTestSuccess(ITestResult result) {
  }

}
