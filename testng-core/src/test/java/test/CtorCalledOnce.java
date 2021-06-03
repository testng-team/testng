package test;

import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

/**
 * this test verifys that the test class is instantiated exactly once
 * regardless of how many test methods we have, showing that TestNG
 * semantics is quite different from JUnit
 */
public class CtorCalledOnce {
  public static int instantiated = 0;
  public CtorCalledOnce() {
    instantiated++;
  }

  @Test
  public void testMethod1(){
    assert instantiated == 1 : "Expected 1, was invoked " + instantiated + " times";
  }

  @Test
  public void testMethod2(){
    assert instantiated == 1 : "Expected 1, was invoked " + instantiated + " times";
  }

  @Test
  public void testMethod3(){
    assert instantiated == 1 : "Expected 1, was invoked " + instantiated + " times";
  }

  @AfterTest
  public void afterTest() {
    instantiated = 0;
  }

}