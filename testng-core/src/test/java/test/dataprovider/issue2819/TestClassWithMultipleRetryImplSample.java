package test.dataprovider.issue2819;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassWithMultipleRetryImplSample {

  @Test(dataProvider = "dp")
  public void sampleTest(int ignored) {}

  @DataProvider(name = "dp", retryUsing = SimpleRetry.class)
  public Object[][] getTestData() {
    if (shouldSimulateFailureForGetTestData()) {
      throw new RuntimeException("Simulating a failure");
    }
    return new Object[][] {{1}};
  }

  @Test(dataProvider = "anotherDP")
  public void anotherSampleTest(int ignored) {}

  @DataProvider(name = "anotherDP", retryUsing = SimpleRetry.class)
  public Object[][] getMoreTestData() {
    if (shouldSimulateFailureForMoreTestData()) {
      throw new RuntimeException("Simulating another failure");
    }
    return new Object[][] {{200}};
  }

  private int moreTestDataCounter = 0;

  private boolean shouldSimulateFailureForMoreTestData() {
    return moreTestDataCounter++ < 2;
  }

  private int testDataCounter = 0;

  private boolean shouldSimulateFailureForGetTestData() {
    return testDataCounter++ < 2;
  }
}
