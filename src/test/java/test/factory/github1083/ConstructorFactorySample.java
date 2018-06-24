package test.factory.github1083;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class ConstructorFactorySample {

  public static final List<String> parameters = new ArrayList<>();

  private final String parameter;

  @Factory(indices = 1, dataProvider = "dp")
  public ConstructorFactorySample(String parameter) {
    this.parameter = parameter;
  }

  @Test
  public void test() {
    parameters.add(parameter);
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {new Object[] {"foo"}, new Object[] {"bar"}};
  }
}
