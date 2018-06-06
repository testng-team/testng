package test.retryAnalyzer.issue1697;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DatadrivenSample {
  private boolean flag = true;

  @Test(dataProvider = "dp", retryAnalyzer = RetryForDataDrivenTest.class)
  public void testMethod(int data) {
    if (data == 1 && flag) {
      flag = false;
      Assert.fail();
    }
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}
