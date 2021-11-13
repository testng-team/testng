package test.reports;

import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;

/** Listener that calls Reporter.log */
public class ReporterLogListener extends TestListenerAdapter {

  @Override
  public void onTestSuccess(ITestResult result) {
    Reporter.log("Listener: onTestSuccess");
    super.onTestSuccess(result);
  }

  @Override
  public void onTestFailure(ITestResult result) {
    Reporter.log("Listener: onTestFailure");
    super.onTestFailure(result);
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    Reporter.log("Listener: onTestSkipped");
    super.onTestSkipped(result);
  }
}
