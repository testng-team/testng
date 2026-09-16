package org.testng.parameters.samples.resolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Three rows, the row cached on retry (the default), and a resolver that throws on its second
 * resolution -- which is the retry of the first row. The other two rows must still run.
 */
public class RetryWithCachedRowAndFailingResolverSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"a"}, {"b"}, {"c"}};
  }

  @Test(dataProvider = "dp", retryAnalyzer = RetryRereadingItsRowSample.OnceRetry.class)
  public void test(@FromResolver CustomObject custom, String row) {
    ParameterRecorder.record("test", custom, row);
    if ("a".equals(row)) {
      throw new AssertionError("first row fails once");
    }
  }
}
