package test.dataprovider;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class DataProviderRetryAnalyzer implements IRetryAnalyzer {
  private int currentTry = 0;
  private int maxreruntimes = 1;

  @Override
  public boolean retry(ITestResult result) {
    if (currentTry < maxreruntimes) {
      ++currentTry;

      return true;
    }
    return false;
  }
}
