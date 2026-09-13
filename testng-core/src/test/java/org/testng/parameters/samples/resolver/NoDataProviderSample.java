package org.testng.parameters.samples.resolver;

import org.testng.annotations.Test;

public class NoDataProviderSample {

  @Test
  public void test(@FromResolver CustomObject custom) {
    ParameterRecorder.record("test", custom);
  }
}
