package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FailedDataProviderSample {
  @DataProvider
  public Object[][] dp() {
    return new Integer[][] {
        new Integer[] { 1 },
        new Integer[] { 2 },
        new Integer[] { 3 },
    };
  }

  @Test(dataProvider = "dp")
  public void f(int n) {
    FailedDataProviderTest.m_total += n;
    if (n == 2) {
      throw new RuntimeException("Failed")  ;
    }
  }

}
