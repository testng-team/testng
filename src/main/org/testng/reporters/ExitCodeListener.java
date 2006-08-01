package org.testng.reporters;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;

/**
 * A very simple <code>ITestListener</code> used by the TestNG runner to 
 * find out the exit code.
 * 
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class ExitCodeListener implements ITestListener {
  private TestNG m_mainRunner;
  
  public ExitCodeListener() {
    m_mainRunner = TestNG.getDefault();
  }

  public void onTestFailure(ITestResult result) {
    m_mainRunner.setHasFailure(true);
  }

  public void onTestSkipped(ITestResult result) {
    m_mainRunner.setHasSkip(true);
  }

  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    m_mainRunner.setHasFailureWithinSuccessPercentage(true);
  }

  public void onTestSuccess(ITestResult result) {
  }

  public void onStart(ITestContext context) {
  }

  public void onFinish(ITestContext context) {
  }

  public void onTestStart(ITestResult result) {
  }
}
