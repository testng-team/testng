package test.factory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactoryDataProviderSample extends BaseFactorySample {

  @Factory(dataProvider = "dp")
  public FactoryDataProviderSample(int n) {
    super(n);
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {
      new Object[] {41}, new Object[] {42},
    };
  }

  @Test
  public void f() {}
}
