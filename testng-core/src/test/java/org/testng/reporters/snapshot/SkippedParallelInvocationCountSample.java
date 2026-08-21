package org.testng.reporters.snapshot;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * The parallel-data-provider twin of {@link SkippedInvocationCountSample}. Which of the two
 * cancellation loops runs is the only difference between them, and it is not something a listener
 * capturing an announcement can see, so both are held to the same invariant.
 */
public class SkippedParallelInvocationCountSample {

  @DataProvider(name = "row", parallel = true)
  public static Object[][] row() {
    return new Object[][] {{"only-row"}};
  }

  @Test(dataProvider = "row", invocationCount = 3, skipFailedInvocations = true)
  public void cancelled(String row) {
    throw new IllegalStateException("this test fails on purpose");
  }
}
