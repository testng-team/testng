package test.dataprovider;

import org.testng.annotations.Factory;

public class TestInstanceFactory {
  @Factory
  public Object[] init() {
    return new Object[] {
        new TestInstanceTest(1),
        new TestInstanceTest(2)
    };
  }


}
