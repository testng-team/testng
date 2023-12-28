package test.retryAnalyzer.issue2148;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

  private int counter = 0;
  private static final int RETRY_LIMIT = 1;

  @Override
  public boolean retry(ITestResult result) {
    if (counter < RETRY_LIMIT) {
      counter++;
      return true;
    }
    return false;
  }
}
