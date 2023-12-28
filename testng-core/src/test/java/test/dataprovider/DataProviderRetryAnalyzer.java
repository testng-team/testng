package test.dataprovider;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class DataProviderRetryAnalyzer implements IRetryAnalyzer {
  private int currentTry = 0;
  private static final int MAXRERUNTIMES = 1;

  @Override
  public boolean retry(ITestResult result) {
    if (currentTry < MAXRERUNTIMES) {
      ++currentTry;

      return true;
    }
    return false;
  }
}
