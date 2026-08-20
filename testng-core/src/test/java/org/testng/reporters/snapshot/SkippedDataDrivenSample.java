package org.testng.reporters.snapshot;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Under {@code reportAllDataDrivenTestsAsSkipped}, a data-driven method skipped by a dependency
 * gets one result per row -- announced before it is given its values, so there is nothing for a
 * reporter to capture and it has to fall back to reading the result.
 */
public class SkippedDataDrivenSample {

  @Test
  public void failing() {
    throw new IllegalStateException("this test fails on purpose");
  }

  @DataProvider(name = "rows")
  public static Object[][] rows() {
    return new Object[][] {{"first"}, {"second"}};
  }

  @Test(dataProvider = "rows", dependsOnMethods = "failing")
  public void skipped(String row) {}
}
