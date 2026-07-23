package test.retryAnalyzer.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderWithStringArraySample {
  private static int countWithStringArray = 3;

  @DataProvider(name = "getTestData")
  public Object[][] getTestData() {
    return new String[][] {new String[] {"abc1", "cdf1"}};
  }

  // Test with string array of objects in data-provider with end result as true after 3 successful
  // retry attempts
  @Test(dataProvider = "getTestData", retryAnalyzer = DataProviderRetryAnalyzer.class)
  public void test(String... values) {
    assertThat(countWithStringArray--)
        .withFailMessage(
            "Test execution is not"
                + "successful after 3 retry attempts configured in retryAnalyzer for this data "
                + Arrays.toString(values))
        .isEqualTo(0);
  }
}
