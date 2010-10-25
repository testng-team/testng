package org.testng.internal.conflistener;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class FailingBeforeTest {
  @BeforeSuite
  public void passingBeforeSuite() {
  }

  @BeforeTest
  public void beforeTest() {
    throw new RuntimeException("Test exception");
  }

  @Test
  public void dummytest() {
  }
}
