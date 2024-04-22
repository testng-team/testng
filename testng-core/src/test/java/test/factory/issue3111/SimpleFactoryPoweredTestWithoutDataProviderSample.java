package test.factory.issue3111;

import org.testng.Reporter;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class SimpleFactoryPoweredTestWithoutDataProviderSample {

  private final int i;

  public SimpleFactoryPoweredTestWithoutDataProviderSample(int i) {
    this.i = i;
  }

  @Test
  public void test() {
    Reporter.log(Integer.toString(i));
  }

  @Factory
  public static Object[] data() {
    return new Object[] {
      new SimpleFactoryPoweredTestWithoutDataProviderSample(1),
      new SimpleFactoryPoweredTestWithoutDataProviderSample(2),
      new SimpleFactoryPoweredTestWithoutDataProviderSample(3),
    };
  }
}
