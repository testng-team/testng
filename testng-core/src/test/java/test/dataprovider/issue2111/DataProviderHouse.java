package test.dataprovider.issue2111;

import org.testng.annotations.DataProvider;

public class DataProviderHouse {

  @DataProvider
  public static Object[][] getData() {
    return new Object[][] {{1}, {2}, {3}};
  }
}
