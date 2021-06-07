package test.retryAnalyzer.github1519;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class MyAnalyzer implements IRetryAnalyzer {

  @Override
  public boolean retry(ITestResult iTestResult) {
    TestClassSample.messages.add("retry");
    TestClassSample.retry = true;
    return true;
  }
}
