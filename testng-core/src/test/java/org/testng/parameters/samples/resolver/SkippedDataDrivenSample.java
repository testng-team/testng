package org.testng.parameters.samples.resolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** A data driven method held back by a failed dependency: reported per row, never invoked. */
public class SkippedDataDrivenSample {

  @Test
  public void prerequisite() {
    throw new AssertionError("the dependency fails");
  }

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"one"}, {"two"}};
  }

  @Test(dataProvider = "dp", dependsOnMethods = "prerequisite")
  public void test(@FromResolver CustomObject custom, String row) {
    ParameterRecorder.record("test", custom, row);
  }
}
