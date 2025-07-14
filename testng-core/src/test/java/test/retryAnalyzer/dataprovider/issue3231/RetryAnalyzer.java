package test.retryAnalyzer.dataprovider.issue3231;

import java.util.List;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.collections.Lists;

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
