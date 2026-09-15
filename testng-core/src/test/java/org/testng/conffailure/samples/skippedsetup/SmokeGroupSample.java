package org.testng.conffailure.samples.skippedsetup;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/** Marks group smoke as failed. */
public class SmokeGroupSample {

  @BeforeGroups("smoke")
  public void beforeSmoke() {
    throw new IllegalStateException("smoke group setup fails");
  }

  @Test(groups = "smoke")
  public void smokeTest() {}
}
