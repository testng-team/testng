package org.testng.parameters.samples.resolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** A parallel data provider with one row that does not fit the method. No resolver involved. */
public class ParallelBadRowSample {

  @DataProvider(name = "dp", parallel = true)
  public Object[][] dp() {
    return new Object[][] {{"a", 1}, {"b"}, {"c", 3}};
  }

  @Test(dataProvider = "dp")
  public void test(String s, int n) {
    ParameterRecorder.record("test", s, n);
  }
}
