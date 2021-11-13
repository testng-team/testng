package test.retryAnalyzer.github1706;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class LocalRetry implements IRetryAnalyzer {
  @Override
  public boolean retry(ITestResult result) {
    return true;
  }
}
