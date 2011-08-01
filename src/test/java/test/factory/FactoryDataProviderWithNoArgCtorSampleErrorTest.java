package test.factory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class FactoryDataProviderWithNoArgCtorSampleErrorTest extends BaseFactory {

  public FactoryDataProviderWithNoArgCtorSampleErrorTest() {
    super(0);
  }

  @Factory(dataProvider = "dp")
  public FactoryDataProviderWithNoArgCtorSampleErrorTest(int n) {
    super(n);
  }

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] { 45 },
      new Object[] { 46 },
    };
  }
  @Override
  public String toString() {
    return "[FactoryDataProviderWithNoArgCtorSampleErrorTest " + getN() + "]";
  }
}
