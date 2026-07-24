package test.factory.github1131;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class EmptyConstructorSample {

  public static int count;

  @Factory(dataProvider = "dataProvider")
  public EmptyConstructorSample() {
    count++;
  }

  @Test
  public void test() {}

  @DataProvider
  public static Object[][] dataProvider() {
    return new Object[][] {new Object[] {}, new Object[] {}};
  }
}
