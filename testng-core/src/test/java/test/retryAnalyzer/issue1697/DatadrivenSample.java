package test.retryAnalyzer.issue1697;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DatadrivenSample {
  private boolean flag = true;

  @Test(dataProvider = "dp", retryAnalyzer = RetryForDataDrivenTest.class)
  public void testMethod(int data) {
    if (data == 1 && flag) {
      flag = false;
      fail();
    }
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}
