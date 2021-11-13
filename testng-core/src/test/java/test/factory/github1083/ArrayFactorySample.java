package test.factory.github1083;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class ArrayFactorySample {

  public static final List<String> parameters = new ArrayList<>();

  private final String parameter;

  private ArrayFactorySample(String parameter) {
    this.parameter = parameter;
  }

  @Test
  public void test() {
    parameters.add(parameter);
  }

  @Factory(indices = 1)
  public static Object[] arrayFactory() {
    return new Object[] {new ArrayFactorySample("foo"), new ArrayFactorySample("bar")};
  }
}
