package test.invocationcount;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderBase {
  @Test(dataProvider = "dp")
  public void f(Integer n) {
  }

  @DataProvider
  public Object[][] dp() {
    return new Integer[][] {
        new Integer[] { 0 },
        new Integer[] { 1 },
        new Integer[] { 2 },
    };
  }

}
