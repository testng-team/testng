package org.testng.timeout.samples.issue3513;

import org.testng.annotations.Test;

/**
 * Ignores interruption and overruns a suite time-out of a few hundred milliseconds. The method
 * declares no time-out of its own, so only the suite bound can cut the {@code <test>} short.
 */
public class StubbornSample {

  @Test
  public void stubborn() {
    IgnoreInterruption.forMillis(2_000);
  }
}
