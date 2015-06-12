package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Vladislav.Rassokhin
 */
public class DataProviderWithError {
  @Test(dataProvider = "Data", invocationCount = 2)
  public void testShouldSkip() throws Exception {
    Assert.fail();
  }

  @Test(dataProvider = "Data", invocationCount = 2, successPercentage = 10)
  public void testShouldSkipEvenIfSuccessPercentage() throws Exception {
    Assert.fail();
  }

  @DataProvider(name = "Data")
  public static Object[][] Data() {
    throw new RuntimeException("Fail");
  }
}
