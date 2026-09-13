package org.testng.conffailure.samples.retry;

import org.testng.annotations.BeforeMethod;

/** The parent setup passes on the first attempt and fails on the retry. */
public class ParentSetupFailsOnRetrySample {

  static int attempt;

  @BeforeMethod
  public void parentSetup() {
    if (++attempt == 2) {
      throw new IllegalStateException("parentSetup fails on the retry");
    }
  }
}
