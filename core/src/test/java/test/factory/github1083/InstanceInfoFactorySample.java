package test.factory.github1083;

import org.testng.IInstanceInfo;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.internal.InstanceInfo;

import java.util.ArrayList;
import java.util.List;

public class InstanceInfoFactorySample {

  public static final List<String> parameters = new ArrayList<>();

  private final String parameter;

  private InstanceInfoFactorySample(String parameter) {
    this.parameter = parameter;
  }

  @Test
  public void test() {
    parameters.add(parameter);
  }

  @Factory(indices = 1)
  public static IInstanceInfo[] arrayFactory() {
    return new IInstanceInfo[] {
      new InstanceInfo<>(InstanceInfoFactorySample.class, new InstanceInfoFactorySample("foo")),
      new InstanceInfo<>(InstanceInfoFactorySample.class, new InstanceInfoFactorySample("bar"))
    };
  }
}
