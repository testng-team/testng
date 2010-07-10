package test.factory;

import org.testng.annotations.Factory;

public class FactoryBase {

  @Factory
  public Object[] create() {
    return new Object[] {
      new FactoryBaseSampleTest()
    };
  }
}
