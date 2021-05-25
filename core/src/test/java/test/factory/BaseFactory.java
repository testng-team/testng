package test.factory;

import org.testng.annotations.Factory;

public class BaseFactory {

  @Factory
  public Object[] create() {
    return new Object[] {new FactoryBaseSample()};
  }
}
