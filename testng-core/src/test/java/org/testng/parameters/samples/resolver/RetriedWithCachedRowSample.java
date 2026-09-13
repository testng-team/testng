package org.testng.parameters.samples.resolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Retried once, with the data provider row cached (the default) and without any provider. */
public class RetriedWithCachedRowSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"value"}};
  }

  @Test(dataProvider = "dp", retryAnalyzer = RetryRereadingItsRowSample.OnceRetry.class)
  public void withDataProvider(@FromResolver CustomObject custom, String fromDataProvider) {
    ParameterRecorder.record("withDataProvider", custom, fromDataProvider);
    throw new AssertionError("always fails so it retries");
  }

  @Test(retryAnalyzer = RetryRereadingItsRowSample.OnceRetry.class)
  public void withoutDataProvider(@FromResolver CustomObject custom) {
    ParameterRecorder.record("withoutDataProvider", custom);
    throw new AssertionError("always fails so it retries");
  }
}
