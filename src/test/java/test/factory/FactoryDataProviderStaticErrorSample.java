package test.factory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class FactoryDataProviderStaticErrorSample extends BaseFactorySample {

  @Factory(dataProvider = "dp")
  public FactoryDataProviderStaticErrorSample(int n) {
    super(n);
  }

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] {41}, new Object[] {42},
    };
  }
}
