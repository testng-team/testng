package org.testng.parameters.samples.resolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Re-reads its row on retry, and an interceptor blanks that re-read row. */
public class RetryWithBlankedRowSample {

  @DataProvider(name = "dp", cacheDataForTestRetries = false)
  public Object[][] dp() {
    return new Object[][] {{"value"}};
  }

  @Test(dataProvider = "dp", retryAnalyzer = RetryRereadingItsRowSample.OnceRetry.class)
  public void test(@FromResolver CustomObject custom, String row) {
    ParameterRecorder.record("test", custom, row);
    throw new AssertionError("always fails so it retries");
  }
}
