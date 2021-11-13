package test.retryAnalyzer;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Issue217TestClassSampleWithOneDataProvider {
  @Test(dataProvider = "dp")
  public void testMethod(int i) {
    Assert.assertTrue(i > 0);
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    throw new RuntimeException("Simulating a failure");
  }
}
