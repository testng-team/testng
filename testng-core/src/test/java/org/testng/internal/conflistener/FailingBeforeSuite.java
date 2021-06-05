package org.testng.internal.conflistener;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class FailingBeforeSuite {
  @BeforeSuite
  public void beforeSuite() {
    throw new RuntimeException("Test exception");
  }

  @Test
  public void dummytest() {}
}
