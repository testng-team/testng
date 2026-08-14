package test.factory.issue3111;

import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class SimpleFactoryPoweredTestWithIndicesSample {

  private final int i;

  @Factory(
      dataProvider = "data",
      indices = {1})
  public SimpleFactoryPoweredTestWithIndicesSample(int i) {
    this.i = i;
  }

  @Test
  public void test() {
    Reporter.log(Integer.toString(i));
  }

  @DataProvider
  public static Object[][] data() {
    return new Object[][] {{1}, {2}, {3}};
  }
}
