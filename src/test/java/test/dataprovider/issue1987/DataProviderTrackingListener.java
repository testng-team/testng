package test.dataprovider.issue1987;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class DataProviderTrackingListener implements ITestListener {
  private ITestResult result;

  @Override
  public void onTestStart(ITestResult result) {
    this.result = result;
  }

  public ITestResult getResult() {
    return result;
  }
}
