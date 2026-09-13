package org.testng.timeout.samples.issue1333;

import org.testng.annotations.Test;

/**
 * Overruns any time-out of a second or so, and declares none of its own, so that whatever bound it
 * ends up under is the one inherited from the XML -- or nothing at all.
 */
public class SlowSample {

  @Test
  public void slow() throws InterruptedException {
    Thread.sleep(3_000);
  }
}
