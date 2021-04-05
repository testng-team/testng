package test.factory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;

public class InterleavingFactorySample {
  @Factory
  public Object[] factory() {
    return new Object[] {new InterleavingSample(1), new InterleavingSample(2)};
  }

  @BeforeClass
  public void beforeB() {}
}
