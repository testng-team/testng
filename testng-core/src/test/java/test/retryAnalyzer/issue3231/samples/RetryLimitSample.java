package test.retryAnalyzer.issue3231.samples;

import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RetryLimitSample {

  public static class MyRetry implements IRetryAnalyzer {
    private int count = 0;

    /**
     * Decides whether a failed test should be retried based on an internal attempt counter.
     *
     * Allows one retry (two total attempts) for the test.
     *
     * @param result the test result under consideration
     * @return `true` if the test should be retried (attempt count is less than 2), `false` otherwise
     */
    @Override
    public boolean retry(ITestResult result) {
      count++;
      return count < 2; // Retry once (total 2 attempts)
    }
  }

  /**
   * Provides test data with two rows, each containing the string "a".
   *
   * @return a two-dimensional Object array with two rows: {"a"} and {"a"}
   */
  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"a"}, {"a"}};
  }

  /**
   * A parametrized test that always fails to demonstrate retry behavior for each provided input.
   *
   * @param s the input value supplied by the "dp" data provider
   */
  @Test(dataProvider = "dp", retryAnalyzer = MyRetry.class)
  public void test(String s) {
    Assert.fail("fail");
  }
}