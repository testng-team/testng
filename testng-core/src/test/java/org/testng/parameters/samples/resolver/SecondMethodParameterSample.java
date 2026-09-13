package org.testng.parameters.samples.resolver;

import java.lang.reflect.Method;
import org.testng.annotations.Test;

/**
 * Two {@link Method} parameters. TestNG injects only the first; the second has never been its to
 * supply, so a resolver may claim it.
 */
public class SecondMethodParameterSample {

  @Test
  public void test(Method current, @FromResolver Method resolved) {
    ParameterRecorder.record("test", current, resolved);
  }
}
