package test.listeners;

import org.testng.IClassListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestAndClassListener implements ITestListener, IClassListener {

  private int beforeClassCount = 0;
  private int afterClassCount = 0;

  @Override
  public void onBeforeClass(ITestClass testClass) {
    beforeClassCount++;
  }

  @Override
  public void onAfterClass(ITestClass testClass) {
    afterClassCount++;
  }

  @Override
  public void onTestStart(ITestResult result) {}

  @Override
  public void onTestSuccess(ITestResult result) {}

  @Override
  public void onTestFailure(ITestResult result) {}

  @Override
  public void onTestSkipped(ITestResult result) {}

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}

  @Override
  public void onStart(ITestContext context) {}

  @Override
  public void onFinish(ITestContext context) {}

  public int getBeforeClassCount() {
    return beforeClassCount;
  }

  public int getAfterClassCount() {
    return afterClassCount;
  }
}
