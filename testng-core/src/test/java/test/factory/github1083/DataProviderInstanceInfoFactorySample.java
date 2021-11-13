package test.factory.github1083;

import java.util.ArrayList;
import java.util.List;
import org.testng.IInstanceInfo;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.internal.InstanceInfo;

public class DataProviderInstanceInfoFactorySample {

  public static final List<String> parameters = new ArrayList<>();

  private final String parameter;

  private DataProviderInstanceInfoFactorySample(String parameter) {
    this.parameter = parameter;
  }

  @Test
  public void test() {
    parameters.add(parameter);
  }

  @Factory(indices = 1, dataProvider = "dp")
  public static IInstanceInfo[] arrayFactory(String s) {
    return new IInstanceInfo[] {
      new InstanceInfo<>(
          DataProviderInstanceInfoFactorySample.class, new DataProviderInstanceInfoFactorySample(s))
    };
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {new Object[] {"foo"}, new Object[] {"bar"}};
  }
}
