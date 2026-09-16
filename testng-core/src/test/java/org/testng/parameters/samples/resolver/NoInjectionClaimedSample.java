package org.testng.parameters.samples.resolver;

import java.lang.reflect.Method;
import org.testng.annotations.NoInjection;
import org.testng.annotations.Test;

/**
 * A {@code @NoInjection Method} that a resolver claims -- the half of that contract not yet pinned.
 */
public class NoInjectionClaimedSample {

  @Test
  public void test(@NoInjection @FromResolver Method resolved) {
    ParameterRecorder.record("test", resolved);
  }
}
