package org.testng.internal.conflistener;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FailingBeforeClass {
  @BeforeClass
  public void failingBeforeClass() {
    throw new RuntimeException("expected @BeforeClass failure");
  }

  @Test
  public void testAfterFailingBeforeClass() {}
}
