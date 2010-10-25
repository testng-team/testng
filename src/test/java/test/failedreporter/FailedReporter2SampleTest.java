package test.failedreporter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FailedReporter2SampleTest {
  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] { 0 },
      new Object[] { 1 },
      new Object[] { 2 },
    };
  }

  @Test(dataProvider = "dp")
  public void f1(Integer ip) {
    if (ip == 1) {
      throw new RuntimeException();
    }
  }
}
