package test.factory;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class ArrayEmptyFactorySample {

  @Factory(dataProvider = "values")
  public ArrayEmptyFactorySample(int value) {
  }

  @DataProvider(name = "values")
  public static Object[][] values() {
    return new Object[][]{};
  }

  @Test
  public void test() {
    Assert.fail();
  }
}
