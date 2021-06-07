package test.factory;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactoryFailureNoInstancesSample {

  public static final String METHOD_NAME =
      FactoryFailureNoInstancesSample.class.getName() + ".factory()";

  public FactoryFailureNoInstancesSample(int i) {}

  @Factory
  public static Object[] factory() {
    return new Object[] {};
  }

  @Test
  public void f() {}
}
