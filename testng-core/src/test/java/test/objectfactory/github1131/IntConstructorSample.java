package test.objectfactory.github1131;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

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
