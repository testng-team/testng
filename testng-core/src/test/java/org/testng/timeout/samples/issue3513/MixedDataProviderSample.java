package org.testng.timeout.samples.issue3513;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * The first data-provider row returns at once. The second ignores interruption and overruns a suite
 * time-out of a few hundred milliseconds.
 */
public class MixedDataProviderSample {

  @DataProvider
  public Object[][] rows() {
    return new Object[][] {{"fast"}, {"slow"}};
  }

  @Test(dataProvider = "rows")
  public void mixedRows(String row) {
    if ("fast".equals(row)) {
      return;
    }
    IgnoreInterruption.forMillis(2_000);
  }
}
