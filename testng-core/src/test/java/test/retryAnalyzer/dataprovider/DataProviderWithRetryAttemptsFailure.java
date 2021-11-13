package test.retryAnalyzer.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderWithRetryAttemptsFailure {
  private static int countWithObjectAndStringArrayForFailure = 3;

  @DataProvider(name = "getObjectData")
  public Object[][] getObjectData() {
    return new Object[][] {new Object[] {false, "abc1", "cdf1"}};
  }

  // Test retry-analyzer with end result as failed after 3 successful retry attempts
  @Test(dataProvider = "getObjectData", retryAnalyzer = DataProviderRetryAnalyzer.class)
  public void test(boolean flag, String... values) {
    Assert.assertTrue(
        flag,
        "Test execution is not"
            + "successful after 3 retry attempts configured in retryAnalyzer for this data "
            + values
            + "with boolean flag as "
            + flag);
  }
}
