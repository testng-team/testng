package test.dataprovider;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderWithErrorSample {

  @Test(dataProvider = "Data", invocationCount = 2)
  public void testShouldSkip() {
    fail();
  }

  @Test(dataProvider = "Data", invocationCount = 2, successPercentage = 10)
  public void testShouldSkipEvenIfSuccessPercentage() {
    fail();
  }

  @DataProvider(name = "Data")
  public static Object[][] Data() {
    throw new RuntimeException("Fail");
  }
}
