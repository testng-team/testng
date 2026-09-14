package org.testng.timeout.samples.issue3513;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Records a passing test, then ignores interruption in {@code @AfterMethod}. The suite time-out can
 * fire while that configuration is still running.
 */
public class AfterMethodBlockedSample {

  @Test
  public void recorded() {}

  @AfterMethod
  public void linger() {
    IgnoreInterruption.forMillis(2_000);
  }
}
