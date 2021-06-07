package test.retryAnalyzer.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderWithSingleParam {
  private static int countWithSingleParam1 = 3;
  private static int countWithSingleParam2 = 3;

  @DataProvider(name = "getSingleParam", parallel = true)
  public Object[][] getSingleParam() {
    return new Integer[][] {new Integer[] {1}, new Integer[] {2}};
  }

  // Test retry-analyzer with end result as passed after 3 successful retry attempts while passing 2
  // integer array of objects individually with parallel as true. Since we have two integer arrays,
  // the number of
  // test-cases in this scenario are 2. Verify if tests are passing successfully after 3 successful
  // attempts each.
  @Test(dataProvider = "getSingleParam", retryAnalyzer = DataProviderRetryAnalyzer.class)
  public void test(int param) {
    // If the param is 1 then decrement the counter countWithSingleParam1 and assert true to check
    // if it is 0.
    if (param == 1) {
      Assert.assertTrue(
          countWithSingleParam1-- == 0,
          "Test execution is not"
              + "successful after 3 retry attempts configured in retryAnalyzer for this data "
              + param);
    }

    // If the param is 2 then decrement the counter countWithSingleParam2 and assert true to check
    // if it is 0.
    if (param == 2) {
      Assert.assertTrue(
          countWithSingleParam2-- == 0,
          "Test execution is not"
              + "successful after 3 retry attempts configured in retryAnalyzer for this data "
              + param);
    }
  }
}
