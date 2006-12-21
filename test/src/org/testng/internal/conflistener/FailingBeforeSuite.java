package org.testng.internal.conflistener;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;


/**
 * This class/interface 
 */
public class FailingBeforeSuite {
  @BeforeSuite
  public void beforeSuite() {
    System.out.println("beforeSuite");
    throw new RuntimeException("Test exception");
  }
  
  @Test
  public void dummytest() {
    System.out.println("dummytest");
  }
}
