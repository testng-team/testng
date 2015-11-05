package test.retryAnalyzer.github857;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.collections.Lists;

import java.util.Collections;
import java.util.List;

public class GitHub857Listener extends TestListenerAdapter {

  public static List<ITestResult>
      passedTests =
      Collections.synchronizedList(Lists.<ITestResult>newArrayList());
  public static List<ITestResult>
      failedTests =
      Collections.synchronizedList(Lists.<ITestResult>newArrayList());
  public static List<ITestResult>
      skippedTests =
      Collections.synchronizedList(Lists.<ITestResult>newArrayList());
  public static List<ITestResult>
      failedButWSPerTests =
      Collections.synchronizedList(Lists.<ITestResult>newArrayList());

  @Override
  public void onTestSuccess(ITestResult result) {
    passedTests.add(result);
  }

  @Override
  public void onTestFailure(ITestResult result) {
    failedTests.add(result);
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    skippedTests.add(result);
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    failedButWSPerTests.add(result);
  }

  @Override
  public void onTestStart(ITestResult result) {}

  @Override
  public void onStart(ITestContext context) {}

  @Override
  public void onFinish(ITestContext context) {}
}
