package test.factory;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactoryFailureSampleTest {

  @Factory
  public Object[] factory() {
    throw new NullPointerException();
  }

  @Test
  public void f() {
  }
}
