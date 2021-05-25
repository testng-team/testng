package test.factory.nested;

import org.testng.annotations.Factory;

public abstract class BaseFactorySample {

  public abstract AbstractBaseSample buildTest();

  @Factory
  public Object[] createObjects() {
    AbstractBaseSample test = buildTest();
    return new Object[] {test};
  }
}
