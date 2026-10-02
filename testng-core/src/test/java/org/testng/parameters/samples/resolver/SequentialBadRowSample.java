package org.testng.parameters.samples.resolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** A sequential data provider with one row that does not fit the method. */
public class SequentialBadRowSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"a", 1}, {"b"}, {"c", 3}};
  }

  @Test(dataProvider = "dp")
  public void test(String s, int n) {
    ParameterRecorder.record("test", s, n);
  }
}
