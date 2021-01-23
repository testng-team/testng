package test.factory.github1131;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class EmptyConstructorSample {

  public static int count = 0;

  @Factory(dataProvider = "dataProvider")
  public EmptyConstructorSample() {
    count++;
  }

  @DataProvider
  public static Object[][] dataProvider() {
    return new Object[][]{new Object[]{}, new Object[]{}};
  }

  @Test
  public void test() {
  }
}
