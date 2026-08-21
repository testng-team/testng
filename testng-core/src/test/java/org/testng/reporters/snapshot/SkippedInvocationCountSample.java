package org.testng.reporters.snapshot;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A method whose first invocation fails, cancelling the {@code invocationCount}s that were still to
 * come. Each of those would have re-run the row the failed one ran with, so that is the value they
 * are announced -- and reported -- with.
 */
public class SkippedInvocationCountSample {

  @DataProvider(name = "row")
  public static Object[][] row() {
    return new Object[][] {{"only-row"}};
  }

  @Test(dataProvider = "row", invocationCount = 3, skipFailedInvocations = true)
  public void cancelled(String row) {
    throw new IllegalStateException("this test fails on purpose");
  }
}
