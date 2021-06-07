package test.retryAnalyzer.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ComplexDataProviderWithObjectAndArraySample {
  private static int countWithObjectAndStringArrayForSuccess = 3;

  @DataProvider(name = "getObjectData")
  public Object[][] getObjectData() {
    return new Object[][] {new Object[] {false, "abc1", "cdf1"}};
  }

  // Test retry-analyzer with complex data-provider end result as passed after 3 successful retry
  // attempts
  @Test(dataProvider = "getObjectData", retryAnalyzer = DataProviderRetryAnalyzer.class)
  public void test(boolean flag, String... values) {
    Assert.assertTrue(
        countWithObjectAndStringArrayForSuccess-- == 0,
        "Test execution is not"
            + "successful after 3 retry attempts configured in retryAnalyzer for this data "
            + values
            + "with boolean flag as "
            + flag);
  }
}
