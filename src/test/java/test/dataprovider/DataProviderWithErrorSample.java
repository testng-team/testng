package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderWithErrorSample {

  @Test(dataProvider = "Data", invocationCount = 2)
  public void testShouldFail() {
    Assert.fail();
  }

  @Test(dataProvider = "Data", invocationCount = 2, successPercentage = 10)
  public void testShouldFailEvenIfSuccessPercentage() {
    Assert.fail();
  }

  @DataProvider(name = "Data")
  public static Object[][] Data() {
    throw new RuntimeException("Fail");
  }
}
