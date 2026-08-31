package test.inject.parameterresolver;

import org.testng.annotations.Optional;
import org.testng.annotations.Test;

/**
 * An {@code @Optional} on the parameter a resolver owns must not decide anything about the
 * parameters it does not own -- {@code notResolvable} is still nobody's to supply.
 */
public class OptionalOnResolvedParameterSample {

  @Test
  public void test(@FromResolver @Optional("ignored") CustomObject custom, int notResolvable) {
    ParameterRecorder.record("test", custom, notResolvable);
  }
}
