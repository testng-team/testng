package test.factory;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class ArrayEmptyFactorySample {

  @DataProvider(name = "values")
  public static Object[][] values() {
    return new Object[][] {};
  }

  @Factory(dataProvider = "values")
  public ArrayEmptyFactorySample(int value) {}

  @Test
  public void test() {
    fail();
  }
}
