package org.testng.parameters.samples.resolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResolvedBeforeDataProviderSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"value"}};
  }

  @Test(dataProvider = "dp")
  public void test(@FromResolver CustomObject custom, String fromDataProvider) {
    ParameterRecorder.record("test", custom, fromDataProvider);
  }
}
