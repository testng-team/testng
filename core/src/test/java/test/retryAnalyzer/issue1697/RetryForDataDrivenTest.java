package test.retryAnalyzer.issue1697;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryForDataDrivenTest implements IRetryAnalyzer {
  private int counter = 0;

  @Override
  public boolean retry(ITestResult result) {
    if (counter++ < 2) {
      return true;
    }
    return false;
  }
}
