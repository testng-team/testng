package org.testng.timeout.samples.issue3513;

import org.testng.annotations.Test;

/**
 * One method an interceptor drops, and one that ignores interruption and overruns a suite time-out.
 * The dropped method must not be reported as timed out.
 */
public class InterceptorSample {

  @Test
  public void dropped() {}

  @Test
  public void stubborn() {
    IgnoreInterruption.forMillis(2_000);
  }
}
