package org.testng.parameters.samples.resolver;

import org.testng.annotations.Test;

/** An {@code Object[]} parameter a resolver would like to own. TestNG fills those itself. */
public class ObjectArrayClaimedSample {

  @Test
  public void test(@FromResolver Object[] values) {
    ParameterRecorder.record("test", new Object[] {values});
  }
}
