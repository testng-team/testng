package test.factory;

import org.testng.annotations.Factory;

public class DoubleFactory {

  @Factory
  public Object[] factory1() {
    return new Object[] {new FactoryBaseSample(1), new FactoryBaseSample(2)};
  }

  @Factory
  public Object[] factory2() {
    return new Object[] {new FactoryBaseSample(3), new FactoryBaseSample(4)};
  }
}
