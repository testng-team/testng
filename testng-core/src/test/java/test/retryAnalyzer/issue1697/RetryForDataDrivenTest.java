package test.retryAnalyzer.issue1697;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryForDataDrivenTest implements IRetryAnalyzer {
  private int counter;

  @Override
  public boolean retry(ITestResult result) {
    return counter++ < 2;
  }
}
