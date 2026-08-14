package test.factory.issue3111;

import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class SimpleFactoryPoweredTestSample {

  private final int i;

  @Factory(dataProvider = "data")
  public SimpleFactoryPoweredTestSample(int i) {
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
