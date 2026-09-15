package org.testng.conffailure.samples.retry;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Row 1 fails once and is retried. Row 2's setup fails. Row 3 must then be skipped, exactly as it
 * is when row 1 does not fail: the retry of row 1 must not exempt row 3.
 */
public class DataProviderRowsSample {

  private boolean row1Failed;

  @DataProvider
  public Object[][] rows() {
    return new Object[][] {{1}, {2}, {3}};
  }

  @BeforeMethod
  public void setup(Object[] params) {
    if ((Integer) params[0] == 2) {
      throw new IllegalStateException("setup fails for row 2");
    }
  }

  @Test(dataProvider = "rows", retryAnalyzer = RetryOnce.class)
  public void t(int row) {
    if (row == 1 && !row1Failed) {
      row1Failed = true;
      throw new AssertionError("row 1 fails on its first attempt only");
    }
  }
}
