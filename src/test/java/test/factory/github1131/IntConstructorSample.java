package test.factory.github1131;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class IntConstructorSample {

  public static final List<Integer> parameters = new ArrayList<>();

  @Factory(dataProvider = "dataProvider")
  public IntConstructorSample(int parameter) {
    parameters.add(parameter);
  }

  @Test
  public void test() {}

  @DataProvider
  public static Object[][] dataProvider() {
    return new Object[][] {new Object[] {1}, new Object[] {2}};
  }
}
