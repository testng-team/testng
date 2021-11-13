package test.retryAnalyzer.issue1697;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class SimpleRetrier implements IRetryAnalyzer {
  @Override
  public boolean retry(ITestResult result) {
    return true;
  }
}
