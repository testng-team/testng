package org.testng.parameters.samples.resolver;

import org.testng.annotations.Optional;
import org.testng.annotations.Test;

/**
 * An {@code Object[]} slot that TestNG fills itself, declared after a parameter a resolver owns.
 */
public class ObjectArrayAfterResolvedSample {

  @Test
  public void test(@FromResolver CustomObject custom, @Optional("x") String s, Object[] values) {
    ParameterRecorder.record("test", custom, s, values);
  }
}
