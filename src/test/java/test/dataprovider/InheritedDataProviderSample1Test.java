package test.dataprovider;

import org.testng.annotations.DataProvider;

public class InheritedDataProviderSample1Test {

  @DataProvider
  static public Object[][] dp() {
    return new Object[][] {
      new Object[] { "a" }
    };
  }
}
