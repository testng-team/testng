package org.testng.parameters.samples.resolver;

import org.testng.annotations.Test;

/** The same shape with nothing to claim it: what TestNG does with {@code Object[]} on its own. */
public class ObjectArrayControlSample {

  @Test
  public void test(Object[] values) {
    ParameterRecorder.record("test", new Object[] {values});
  }
}
