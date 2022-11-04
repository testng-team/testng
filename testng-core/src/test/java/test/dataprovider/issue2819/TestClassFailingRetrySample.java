package test.dataprovider.issue2819;

import org.testng.IDataProviderMethod;
import org.testng.IRetryDataProvider;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassFailingRetrySample {

  @Test(dataProvider = "dp")
  public void testMethod(int ignored) {}

  @DataProvider(name = "dp", retryUsing = DonotRetry.class)
  public Object[][] getData() {
    throw new RuntimeException("problem");
  }

  public static class DonotRetry implements IRetryDataProvider {

    @Override
    public boolean retry(IDataProviderMethod dataProvider) {
      return false;
    }
  }
}
