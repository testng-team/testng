package test.retryAnalyzer.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
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
    assertThat(countWithObjectAndStringArrayForSuccess--)
        .withFailMessage(
            "Test execution is not"
                + "successful after 3 retry attempts configured in retryAnalyzer for this data "
                + Arrays.toString(values)
                + "with boolean flag as "
                + flag)
        .isEqualTo(0);
  }
}
