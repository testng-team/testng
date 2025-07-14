package test.retryAnalyzer.dataprovider.issue3231;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

  private static final int DEFAULT_MAX_RETRY_COUNT = 2;
  private int retryCount = 0;

  @Override
  public boolean retry(ITestResult result) {
    if (retryCount < DEFAULT_MAX_RETRY_COUNT) {
      retryCount++;
      return true;
    }
    return false;
  }
}
