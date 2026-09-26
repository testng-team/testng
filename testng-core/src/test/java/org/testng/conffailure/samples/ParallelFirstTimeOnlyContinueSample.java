package org.testng.conffailure.samples;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A {@code firstTimeOnly} setup is shared by every parallel data-provider row. If it fails, every
 * row should skip, including under {@code configFailurePolicy=CONTINUE}.
 */
public class ParallelFirstTimeOnlyContinueSample {

  @BeforeMethod(firstTimeOnly = true)
  public void sharedSetup() {
    throw new IllegalStateException("shared setup fails");
  }

  @DataProvider(name = "rows", parallel = true)
  public Object[][] rows() {
    return new Object[][] {{0}, {1}};
  }

  @Test(dataProvider = "rows")
  public void t(int row) {}
}
