package test.dataprovider;

import org.testng.annotations.DataProvider;

public class InheritedDataProvider {

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {{"a"}};
  }
}
