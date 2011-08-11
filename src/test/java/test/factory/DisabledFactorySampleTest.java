package test.factory;

import org.testng.annotations.Factory;

public class DisabledFactorySampleTest {

  @Factory(enabled = false)
  public Object[] factory() {
    return new Object[] { new MyTest() };
  }
}
