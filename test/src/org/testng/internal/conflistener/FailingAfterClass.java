package org.testng.internal.conflistener;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * This class/interface does XXX
 */
public class FailingAfterClass {
  @AfterClass
  public void failingAfterClass() {
    throw new RuntimeException("expected @AfterClass failure");
  }

  @Test
  public void testBeforeFailingAfterClass() {}
}
