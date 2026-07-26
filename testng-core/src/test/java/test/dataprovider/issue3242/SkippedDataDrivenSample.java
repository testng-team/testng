package test.dataprovider.issue3242;

import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A sample for the GITHUB-3242 scenario where the codebase <em>declares</em> as many data-driven
 * tests as there are threads, but those tests are filtered from execution at runtime (each data-row
 * throws a {@link SkipException}). The data-driven tests are still present in the run - which is
 * what used to trip the "[Deadlock condition detected]" guard before the suite even started - so
 * the suite must now start and run its remaining tests, skipping the data-driven ones.
 */
public class SkippedDataDrivenSample {

  @Test
  public void regular() {}

  @Test(dataProvider = "data")
  public void ddt1(int ignored) {
    throw new SkipException("filtered from execution");
  }

  @Test(dataProvider = "data")
  public void ddt2(int ignored) {
    throw new SkipException("filtered from execution");
  }

  @Test(dataProvider = "data")
  public void ddt3(int ignored) {
    throw new SkipException("filtered from execution");
  }

  @Test(dataProvider = "data")
  public void ddt4(int ignored) {
    throw new SkipException("filtered from execution");
  }

  @Test(dataProvider = "data")
  public void ddt5(int ignored) {
    throw new SkipException("filtered from execution");
  }

  @DataProvider(name = "data", parallel = true)
  public Object[][] data() {
    return new Object[][] {{1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}};
  }
}
