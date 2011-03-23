package test.factory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class FactoryDataProviderStaticSampleErrorTest extends BaseFactory {
  @Factory(dataProvider = "dp")
  public FactoryDataProviderStaticSampleErrorTest(int n) {
    super(n);
  }

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] { 41 },
      new Object[] { 42 },
    };
  }

}
