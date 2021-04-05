package test.factory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class FactoryDataProviderWithNoArgCtorErrorSample extends BaseFactorySample {

  public FactoryDataProviderWithNoArgCtorErrorSample() {
    super(0);
  }

  @Factory(dataProvider = "dp")
  public FactoryDataProviderWithNoArgCtorErrorSample(int n) {
    super(n);
  }

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] {45}, new Object[] {46},
    };
  }
}
