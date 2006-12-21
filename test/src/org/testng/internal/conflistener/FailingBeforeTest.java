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
    System.out.println("passingBeforeSuite");
  }
  
  @BeforeTest
  public void beforeTest() {
    System.out.println("beforeTest");
    throw new RuntimeException("Test exception");
  }
  
  @Test
  public void dummytest() {
    System.out.println("dummytest");
  }
}
