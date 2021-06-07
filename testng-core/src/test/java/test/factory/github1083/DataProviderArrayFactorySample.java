package test.factory.github1083;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class DataProviderArrayFactorySample {

  public static final List<String> parameters = new ArrayList<>();

  private final String parameter;

  private DataProviderArrayFactorySample(String parameter) {
    this.parameter = parameter;
  }

  @Test
  public void test() {
    parameters.add(parameter);
  }

  @Factory(indices = 1, dataProvider = "dp")
  public static Object[] arrayFactory(String s) {
    return new Object[] {new DataProviderArrayFactorySample(s)};
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {new Object[] {"foo"}, new Object[] {"bar"}};
  }
}
