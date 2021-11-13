package org.testng.internal.conflistener;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

public class FailingAfterSuite {
  @AfterSuite(alwaysRun = true)
  public void afterSuite() {
    throw new RuntimeException("Test exception");
  }

  @Test
  public void dummytest() {}
}
