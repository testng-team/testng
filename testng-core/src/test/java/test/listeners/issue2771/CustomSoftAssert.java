package test.listeners.issue2771;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class CustomSoftAssert implements ITestListener {
  @Override
  public void onTestSuccess(ITestResult result) {
    result.setStatus(ITestResult.FAILURE);
    result.setThrowable(new AssertionError("There have been some failed soft asserts"));
  }
}
