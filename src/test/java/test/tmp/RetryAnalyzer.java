package test.tmp;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

  @Override
  public boolean retry(ITestResult result) {
    System.out.println("retry()");
    return true;
  }

}
