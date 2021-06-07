package test.reports;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GitHub148Sample {

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {{1}, {2}, {3}};
  }

  @Test(dataProvider = "dp")
  public void testMethod(int test) {
    if (test == 3) {
      throw new RuntimeException("Test Case: " + test + " failed!");
    }
  }
}
