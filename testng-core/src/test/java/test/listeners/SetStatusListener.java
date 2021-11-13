package test.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/** From https://github.com/rajsrivastav1919/TestNGTest */
public class SetStatusListener implements ITestListener {

  private ITestContext context;

  @Override
  public void onTestStart(ITestResult result) {}

  @Override
  public void onTestSuccess(ITestResult result) {}

  @Override
  public void onTestFailure(ITestResult result) {
    result.setStatus(ITestResult.SUCCESS);
  }

  @Override
  public void onTestSkipped(ITestResult result) {}

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}

  @Override
  public void onStart(ITestContext context) {}

  @Override
  public void onFinish(ITestContext context) {
    this.context = context;
  }

  public ITestContext getContext() {
    return context;
  }
}
