package test.factory;

import org.testng.annotations.Factory;

public class FactoryDataProviderStaticSample extends BaseFactory {

  @Factory(dataProvider = "dp", dataProviderClass = StaticDataProvider.class)
  public FactoryDataProviderStaticSample(int n) {
    super(n);
  }
}
