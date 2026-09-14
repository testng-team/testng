package org.testng.timeout.samples.issue3513;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Two rows share one method and run at the same time. The fast row returns at once. The slow row
 * ignores interruption and overruns a suite time-out of a few hundred milliseconds.
 */
public class ParallelMixedDataProviderSample {

  @DataProvider(parallel = true)
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
