package org.testng.parameters.samples.resolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Three sequential rows. A resolver that throws while building the middle row must fail that row
 * and still run the other two.
 */
public class SequentialResolverFailureSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"a"}, {"b"}, {"c"}};
  }

  @Test(dataProvider = "dp")
  public void test(@FromResolver CustomObject custom, String row) {
    ParameterRecorder.record("test", custom, row);
  }
}
