package test.configuration;

import org.testng.annotations.Factory;

public class SuiteFactoryOnceSample2Test {

  @Factory
  public Object[] factory() {
    return new Object[] { new SuiteFactoryOnceSample1Test(), new SuiteFactoryOnceSample1Test() };
  }
}
