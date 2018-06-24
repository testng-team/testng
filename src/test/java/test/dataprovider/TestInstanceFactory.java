package test.dataprovider;

import org.testng.annotations.Factory;

public class TestInstanceFactory {

  @Factory
  public Object[] init() {
    return new Object[] {new TestInstanceSample(1), new TestInstanceSample(2)};
  }
}
