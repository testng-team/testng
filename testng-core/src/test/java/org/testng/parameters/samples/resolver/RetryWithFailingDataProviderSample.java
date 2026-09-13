package org.testng.parameters.samples.resolver;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A retried data driven method that re-reads its row, whose provider only answers once. No resolver
 * is involved: this is the row itself failing to come back, which reaches the retry through the
 * parameter bag.
 */
public class RetryWithFailingDataProviderSample {

  private static final AtomicInteger CALLS = new AtomicInteger();

  @DataProvider(name = "dp", cacheDataForTestRetries = false)
  public Object[][] dp() {
    if (CALLS.incrementAndGet() > 1) {
      throw new IllegalStateException("provider broke on the retry");
    }
    return new Object[][] {{"value"}};
  }

  @Test(dataProvider = "dp", retryAnalyzer = RetryRereadingItsRowSample.OnceRetry.class)
  public void test(String fromDataProvider) {
    ParameterRecorder.record("test", fromDataProvider);
    throw new AssertionError("always fails so it retries");
  }
}
