package test.reports.issue3418;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactoryOnlySample {

  private final String name;

  @Factory(dataProvider = "dp")
  public FactoryOnlySample(String name) {
    this.name = name;
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {{"alpha"}};
  }

  @Test
  public void test() {
    if (!"alpha".equals(name)) {
      throw new AssertionError(name);
    }
  }
}
