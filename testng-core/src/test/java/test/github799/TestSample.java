package test.github799;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class TestSample {
  int num;

  @Factory(dataProvider = "data")
  public TestSample(int n) {
    num = n;
  }

  @DataProvider
  public static Object[][] data() {
    return new Object[][] {{1}, {2}, {3}, {4}};
  }

  @Test
  public void test() {
    Reporter.log(Integer.toString(num));
    Assert.assertTrue(num > 0);
  }
}
