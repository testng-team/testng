package org.testng.conffailure.samples.skippedsetup;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Alone, c passes: {@code ignoreFailure} covers its own @BeforeClass. It must still pass after D.
 */
public class IgnoredClassFailureSample {

  @BeforeClass(ignoreFailure = true)
  public void beforeClassC() {
    throw new IllegalStateException("C.beforeClass fails, but ignoreFailure is set");
  }

  @Test
  public void c() {}
}
