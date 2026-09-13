package org.testng.factory.samples;

import org.testng.annotations.Factory;

public class FactoryDataProviderStaticSample extends BaseFactorySample {

  @Factory(dataProvider = "dp", dataProviderClass = StaticDataProvider.class)
  public FactoryDataProviderStaticSample(int n) {
    super(n);
  }
}
