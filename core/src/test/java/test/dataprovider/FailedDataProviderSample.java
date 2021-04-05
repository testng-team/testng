package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FailedDataProviderSample {

  @DataProvider
  public Object[][] dp() {
    return new Integer[][] {{1}, {2}, {3}};
  }

  @Test(dataProvider = "dp")
  public void f(int n) {
    if (n == 2) {
      throw new RuntimeException("Failed");
    }
  }
}
