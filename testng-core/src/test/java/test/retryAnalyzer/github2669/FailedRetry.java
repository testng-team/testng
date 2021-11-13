package test.retryAnalyzer.github2669;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class FailedRetry implements IRetryAnalyzer {
  public static int retryCount = 1;
  private static final int maxRetryCount = 3;

  @Override
  public boolean retry(ITestResult iTestResult) {
    // If an exception is thrown, the failure case will be rerun. If it is an assertion error, try
    // again
    if (iTestResult.getThrowable() instanceof AssertionError && retryCount % maxRetryCount != 0) {
      retryCount++;
      return true;
    }
    retryCount = 1;
    return false;
  }
}
