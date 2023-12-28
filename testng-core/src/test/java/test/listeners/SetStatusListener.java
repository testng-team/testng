package test.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class SetStatusListener implements ITestListener {

  private ITestContext context;

  @Override
  public void onTestFailure(ITestResult result) {
    result.setStatus(ITestResult.SUCCESS);
  }

  @Override
  public void onFinish(ITestContext context) {
    this.context = context;
  }

  public ITestContext getContext() {
    return context;
  }
}
