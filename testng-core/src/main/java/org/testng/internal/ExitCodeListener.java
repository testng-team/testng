package org.testng.internal;

import java.util.List;
import org.testng.IExecutionListener;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNGException;
import org.testng.xml.XmlSuite;

public class ExitCodeListener implements ITestListener, IReporter, IExecutionListener {
  private boolean hasTests = false;
  private final ExitCode status = new ExitCode();
  private boolean failIfAllTestsSkipped = false;

  public void failIfAllTestsSkipped() {
    this.failIfAllTestsSkipped = true;
  }

  public ExitCode getStatus() {
    return status;
  }

  public boolean noTestsFound() {
    return !hasTests;
  }

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    for (ISuite suite : suites) {
      for (ISuiteResult suiteResult : suite.getResults().values()) {
        ITestContext context = suiteResult.getTestContext();
        status.computeAndUpdate(context);
      }
    }
  }

  @Override
  public void onTestStart(ITestResult result) {
    this.hasTests = true;
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    this.hasTests = true;
  }

  @Override
  public void onTestFailure(ITestResult result) {
    this.hasTests = true;
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    this.hasTests = true;
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    this.hasTests = true;
  }

  @Override
  public void onStart(ITestContext context) {}

  @Override
  public void onFinish(ITestContext context) {}

  @Override
  public void onExecutionFinish() {
    if (failIfAllTestsSkipped && (getStatus().getExitCode() == ExitCode.SKIPPED)) {
      throw new TestNGException("All tests were skipped. Nothing was run.");
    }
  }
}
