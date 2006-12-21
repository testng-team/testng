package org.testng.internal.conflistener;

import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;


/**
 * This class/interface 
 */
public class FailingAfterTest {
  @AfterTest(alwaysRun=true)
  public void afterTest() {
    System.out.println("afterTest");
    throw new RuntimeException("Test exception");
  }
  
  @AfterTest
  public void skippedAfterTest() {
    System.out.println("skippedAfterTest");
  }
  
  @Test
  public void dummytest() {
    System.out.println("dummytest");
  }
}
