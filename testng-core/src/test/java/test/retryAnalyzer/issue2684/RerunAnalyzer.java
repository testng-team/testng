package test.retryAnalyzer.issue2684;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RerunAnalyzer implements IRetryAnalyzer {

  public static final int maxRetryCount = 1;
  public static int secondTestRetryCount = 0;
  private int retryCount = 0;

  @Override
  public boolean retry(ITestResult iTestResult) {
    if (retryCount < maxRetryCount) {
      retryCount++;
      return true;
    }
    return false;
  }
}
