package test.invocationcount.issue3170;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataDrivenWithSuccessPercentageAndInvocationCountDefinedSample {
  @Test(dataProvider = "test", invocationCount = 10, successPercentage = 90)
  public void sampleTestCase(String string) {
    assertEquals(string, "1");
  }

  @DataProvider(name = "test")
  public Object[][] testProvider() {
    return new Object[][] {{"2"}};
  }
}
