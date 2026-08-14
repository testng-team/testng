package test.listeners.issue3238;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class FailureTrackingListener implements ITestListener {

  @Override
  public void onTestFailure(ITestResult result) {
    BadUtility.doNothing();
  }
}
