package org.testng.timeout.samples.issue3513;

import org.testng.annotations.Test;

/**
 * Sleeps longer than a suite time-out of a few hundred milliseconds, and stops when interrupted.
 * The method declares no time-out of its own.
 */
public class InterruptibleSample {

  @Test
  public void interruptible() throws InterruptedException {
    Thread.sleep(2_000);
  }
}
