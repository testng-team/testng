package test.junit4.issue2792;

import java.util.Objects;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestContextGatheringListener implements IInvokedMethodListener, ITestListener {

  private boolean testContextFoundOnTestStart = false;
  private boolean testContextFoundOnAfterInvocation = false;

  public boolean isTestContextFoundOnAfterInvocation() {
    return testContextFoundOnAfterInvocation;
  }

  public boolean isTestContextFoundOnTestStart() {
    return testContextFoundOnTestStart;
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    testContextFoundOnAfterInvocation = Objects.nonNull(testResult.getTestContext());
  }

  @Override
  public void onTestStart(ITestResult result) {
    testContextFoundOnTestStart = Objects.nonNull(result.getTestContext());
  }
}
