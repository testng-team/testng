package test.factory;

import org.testng.annotations.Factory;

public class FactoryDataProviderStaticSampleTest extends BaseFactory {

  @Factory(dataProvider = "dp", dataProviderClass = DPClass.class)
  public FactoryDataProviderStaticSampleTest(int n) {
    super(n);
  }
}
