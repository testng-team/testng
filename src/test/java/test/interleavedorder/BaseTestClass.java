package test.interleavedorder;

import org.testng.annotations.Test;


public class BaseTestClass {
  @Test
  public void testOne() {
    ppp("test1");
    InterleavedInvocationTest.LOG.add("test1");
  }

  @Test
  public void testTwo() {
    ppp("test2");
    InterleavedInvocationTest.LOG.add("test2");
  }

  private void ppp(String s) {
    if (false) {
      System.out.println(getClass().toString() + " " + s);
    }
  }

}
