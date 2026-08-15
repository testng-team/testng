package test.factory.issue3111;

import org.testng.Reporter;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class SimpleFactoryPoweredTestWithoutDataProviderWithIndicesSample {

  private final int i;

  public SimpleFactoryPoweredTestWithoutDataProviderWithIndicesSample(int i) {
    this.i = i;
  }

  @Test
  public void test() {
    Reporter.log(Integer.toString(i));
  }

  @Factory(indices = {1})
  public static Object[] data() {
    return new Object[] {
      new SimpleFactoryPoweredTestWithoutDataProviderWithIndicesSample(1),
      new SimpleFactoryPoweredTestWithoutDataProviderWithIndicesSample(2),
      new SimpleFactoryPoweredTestWithoutDataProviderWithIndicesSample(3),
    };
  }
}
