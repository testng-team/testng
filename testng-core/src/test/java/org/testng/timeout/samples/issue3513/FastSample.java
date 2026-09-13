package org.testng.timeout.samples.issue3513;

import org.testng.annotations.Test;

/** Finishes at once, so a suite time-out is not about this {@code <test>}. */
public class FastSample {

  @Test
  public void fast() {}
}
