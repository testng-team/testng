package test.listeners.issue2771;

import org.testng.ITestResult;
import org.testng.reporters.ExitCodeListener;

public class CustomSoftAssert extends ExitCodeListener {
  @Override
  public void onTestSuccess(ITestResult result) {
    result.setStatus(ITestResult.FAILURE);
    result.setThrowable(new AssertionError("There have been some failed soft asserts"));
  }
}
