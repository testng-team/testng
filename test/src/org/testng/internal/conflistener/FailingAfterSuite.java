package org.testng.internal.conflistener;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;


/**
 * This class/interface 
 */
public class FailingAfterSuite {
  @AfterSuite(alwaysRun=true)
  public void afterSuite() {
    System.out.println("afterSuite");
    throw new RuntimeException("Test exception");
  }
  
  @Test
  public void dummytest() {
    System.out.println("dummytest");
  }
}
