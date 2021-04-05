package test.listeners;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.IResultListener2;

public class ResultListener implements IResultListener2 {

  public static long m_end = 0;

  @Override
  public void onTestStart(ITestResult result) {
    // TODO Auto-generated method stub

  }

  @Override
  public void beforeConfiguration(ITestResult tr) {
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    m_end = result.getEndMillis();
  }

  @Override
  public void onTestFailure(ITestResult result) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onTestSkipped(ITestResult result) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onStart(ITestContext context) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onFinish(ITestContext context) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onConfigurationSuccess(ITestResult itr) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onConfigurationFailure(ITestResult itr) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onConfigurationSkip(ITestResult itr) {
    // TODO Auto-generated method stub

  }
}
