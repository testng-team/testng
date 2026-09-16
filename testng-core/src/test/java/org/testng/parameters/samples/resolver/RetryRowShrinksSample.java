package org.testng.parameters.samples.resolver;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Three rows, re-read on retry; the second read hands back a row that no longer fits the method at
 * the index being retried. The third row must still run.
 */
public class RetryRowShrinksSample {

  private final AtomicInteger providerCalls = new AtomicInteger();

  @DataProvider(name = "dp", cacheDataForTestRetries = false)
  public Object[][] dp() {
    if (providerCalls.incrementAndGet() == 1) {
      return new Object[][] {{"a", "x"}, {"b", "y"}, {"c", "z"}};
    }
    return new Object[][] {{"a", "x"}, {"b"}, {"c", "z"}};
  }

  @Test(dataProvider = "dp", retryAnalyzer = RetryRereadingItsRowSample.OnceRetry.class)
  public void test(String first, String second) {
    ParameterRecorder.record("test", first, second);
    if ("b".equals(first)) {
      throw new AssertionError("second row fails once");
    }
  }
}
