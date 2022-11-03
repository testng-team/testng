package test.dataprovider.issue2819;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassUsingDataProviderRetrySample {

  private int counter = 0;

  @Test(dataProvider = "dp")
  public void sampleTest(int ignored) {}

  @DataProvider(name = "dp", retryUsing = SimpleRetry.class)
  public Object[][] getTestData() {
    if (shouldSimulateFailure()) {
      throw new RuntimeException("Simulating a failure");
    }
    return new Object[][] {{1}};
  }

  private boolean shouldSimulateFailure() {
    return counter++ < 2;
  }
}
