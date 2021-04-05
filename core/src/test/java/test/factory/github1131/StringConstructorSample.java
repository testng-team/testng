package test.factory.github1131;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class StringConstructorSample {

  public static final List<String> parameters = new ArrayList<>();

  @Factory(dataProvider = "dataProvider")
  public StringConstructorSample(String parameter) {
    parameters.add(parameter);
  }

  @Test
  public void test() {}

  @DataProvider
  public static Object[][] dataProvider() {
    return new Object[][] {new Object[] {"foo"}, new Object[] {"bar"}};
  }
}
