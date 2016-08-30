package test.factory;

import org.testng.annotations.Factory;

public class FactoryDataProviderNotStaticSample extends BaseFactory {

  @Factory(dataProvider = "dp", dataProviderClass = NotStaticDataProvider.class)
  public FactoryDataProviderNotStaticSample(int n) {
    super(n);
  }
}
