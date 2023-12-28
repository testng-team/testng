package org.testng.reporters;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.IResultListener2;

/**
 * A very simple <code>ITestListener</code> used by the TestNG runner to find out the exit code.
 *
 * @deprecated - This class stands deprecated as of TestNG <code>v7.10.0</code>
 */
@Deprecated
public class ExitCodeListener implements IResultListener2 {

  @Override
  public void onTestFailure(ITestResult result) {
    setHasRunTests();
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    setHasRunTests();
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    setHasRunTests();
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    setHasRunTests();
  }

  @Override
  public void onStart(ITestContext context) {
    setHasRunTests();
  }

  @Override
  public void onTestStart(ITestResult result) {
    setHasRunTests();
  }

  private void setHasRunTests() {}

  /** @see org.testng.IConfigurationListener#onConfigurationFailure(org.testng.ITestResult) */
  @Override
  public void onConfigurationFailure(ITestResult itr) {}

  /** @see org.testng.IConfigurationListener#onConfigurationSkip(org.testng.ITestResult) */
  @Override
  public void onConfigurationSkip(ITestResult itr) {}

  /** @see org.testng.IConfigurationListener#onConfigurationSuccess(org.testng.ITestResult) */
  @Override
  public void onConfigurationSuccess(ITestResult itr) {}
}
