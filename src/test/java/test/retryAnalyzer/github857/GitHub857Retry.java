package test.retryAnalyzer.github857;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class GitHub857Retry implements IRetryAnalyzer {

  private static final int MAX_RETRY_COUNT_PER_TEST = 1;
  private int retryCountPerTest = 0;

  @Override
  public boolean retry(ITestResult result) {
    if (retryCountPerTest < MAX_RETRY_COUNT_PER_TEST) {
      retryCountPerTest++;
      return true;
    }
    return false;
  }
}
