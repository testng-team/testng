package org.testng.conffailure.samples.skippedsetup;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Its alwaysRun @BeforeMethod is skipped; that skip must not count as a method-level failure. */
public class SkippedMethodSetupSample {

  @BeforeClass
  public void beforeClassD() {
    throw new IllegalStateException("D.beforeClass fails");
  }

  @BeforeMethod(alwaysRun = true)
  public void beforeMethodD() {}

  @Test
  public void d() {}
}
