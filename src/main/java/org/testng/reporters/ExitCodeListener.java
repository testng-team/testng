package org.testng.reporters;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.internal.IResultListener2;

/** A very simple <code>ITestListener</code> used by the TestNG runner to find out the exit code. */
public class ExitCodeListener implements IResultListener2 {
  private TestNG m_mainRunner;

  public ExitCodeListener() {
    this(TestNG.getDefault());
  }

  public ExitCodeListener(TestNG runner) {
    m_mainRunner = runner;
  }

  @Override
  public void beforeConfiguration(ITestResult tr) {}

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
  public void onFinish(ITestContext context) {}

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
