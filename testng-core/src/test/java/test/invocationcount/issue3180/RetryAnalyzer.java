package test.invocationcount.issue3180;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

  int counter = 0;
  int retryLimit = 3;

  /*
   * Retry method
   */
  public boolean retry(ITestResult result) {
    return counter++ < retryLimit;
  }
}
