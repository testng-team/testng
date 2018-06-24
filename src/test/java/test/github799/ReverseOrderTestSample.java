package test.github799;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class ReverseOrderTestSample {
  int num;

  @Factory(dataProvider = "data")
  public ReverseOrderTestSample(int n) {
    num = n;
  }

  @DataProvider
  public static Object[][] data() {
    return new Object[][] {{4}, {1}, {3}, {2}};
  }

  @Test
  public void test() {
    Reporter.log(Integer.toString(num));
    Assert.assertTrue(num > 0);
  }
}
