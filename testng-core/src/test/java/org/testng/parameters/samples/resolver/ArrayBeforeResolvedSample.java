package org.testng.parameters.samples.resolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * The array is not the last declared parameter, so it is not a trailing array -- whatever the
 * resolver owns after it. A three-value row for it is a mismatch, as it is with no resolver.
 */
public class ArrayBeforeResolvedSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"1", "2", "3"}};
  }

  @Test(dataProvider = "dp")
  public void test(String[] array, @FromResolver CustomObject custom) {
    ParameterRecorder.record("test", array, custom);
  }
}
