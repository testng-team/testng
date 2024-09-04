package test.invocationcount.issue3170;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataDrivenWithSuccessPercentageDefinedSample {
  @Test(dataProvider = "test", successPercentage = 99)
  public void sampleTestCase(String string) {
    assertEquals(string, "1");
  }

  @DataProvider(name = "test")
  public Object[][] testProvider() {
    return new Object[][] {{"1"}, {"2"}, {"3"}, {"4"}};
  }
}
