package org.testng.internal.conflistener;

import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class FailingAfterTest {
  @AfterTest(alwaysRun=true)
  public void afterTest() {
    throw new RuntimeException("Test exception");
  }

  @AfterTest
  public void skippedAfterTest() {
  }

  @Test
  public void dummytest() {
  }
}
