package org.testng.parameters.samples.resolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** A retried data driven method that re-reads its row, whose resolver only answers once. */
public class RetryWithFailingResolverSample {

  @DataProvider(name = "dp", cacheDataForTestRetries = false)
  public Object[][] dp() {
    return new Object[][] {{"value"}};
  }

  @Test(dataProvider = "dp", retryAnalyzer = RetryRereadingItsRowSample.OnceRetry.class)
  public void test(@FromResolver CustomObject custom, String fromDataProvider) {
    ParameterRecorder.record("test", custom, fromDataProvider);
    throw new AssertionError("always fails so it retries");
  }

  @Test
  public void sibling() {
    ParameterRecorder.record("sibling");
  }
}
