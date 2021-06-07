package test.retryAnalyzer.issue1538;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryForIssue1538 implements IRetryAnalyzer {
  @Override
  public boolean retry(ITestResult result) {
    return true;
  }
}
