package test.inject.parameterresolver;

import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A retry that re-reads its data provider row, which is what {@code cacheDataForTestRetries =
 * false} asks for. It carries a control method using only native injection, so the two can be
 * compared.
 */
public class RetryRereadingItsRowSample {

  public static class OnceRetry implements IRetryAnalyzer {
    private boolean retried = false;

    @Override
    public boolean retry(ITestResult result) {
      if (retried) {
        return false;
      }
      retried = true;
      return true;
    }
  }

  @DataProvider(name = "dp", cacheDataForTestRetries = false)
  public Object[][] dp() {
    return new Object[][] {{"value"}};
  }

  // Control: native injection only, no resolver involved.
  @Test(dataProvider = "dp", retryAnalyzer = OnceRetry.class)
  public void nativeControl(String fromDataProvider, ITestContext context) {
    ParameterRecorder.record("nativeControl", fromDataProvider, context);
    throw new AssertionError("always fails so it retries");
  }

  // The new API on the same path.
  @Test(dataProvider = "dp", retryAnalyzer = OnceRetry.class)
  public void withResolver(@FromResolver CustomObject custom, String fromDataProvider) {
    ParameterRecorder.record("withResolver", custom, fromDataProvider);
    throw new AssertionError("always fails so it retries");
  }
}
