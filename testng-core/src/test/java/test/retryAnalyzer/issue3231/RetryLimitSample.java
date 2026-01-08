package test.retryAnalyzer.issue3231;

import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RetryLimitSample {

  public static class MyRetry implements IRetryAnalyzer {
    private int count = 0;

    @Override
    public boolean retry(ITestResult result) {
      count++;
      return count < 2; // Retry once (total 2 attempts)
    }
  }

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"a"}, {"a"}};
  }

  @Test(dataProvider = "dp", retryAnalyzer = MyRetry.class)
  public void test(String s) {
    Assert.fail("fail");
  }
}
