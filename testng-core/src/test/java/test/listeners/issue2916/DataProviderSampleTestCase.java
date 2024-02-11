package test.listeners.issue2916;

import org.testng.annotations.*;

public class DataProviderSampleTestCase {

  @Test(dataProvider = "getData", priority = 1)
  public void testMethod(int ignored) {}

  @DataProvider
  public Object[][] getData() {
    return new Object[][] {{1}};
  }

  @Test(dataProvider = "failingDataProvider", priority = 2)
  public void failingTestMethod(int ignored) {}

  @DataProvider
  public Object[][] failingDataProvider() {
    throw new IllegalStateException("Failing data provider");
  }
}
