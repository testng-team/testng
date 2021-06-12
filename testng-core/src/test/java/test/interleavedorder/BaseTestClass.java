package test.interleavedorder;

import org.testng.annotations.Test;

public class BaseTestClass {
  @Test
  public void testOne() {
    InterleavedInvocationTest.LOG.add("test1");
  }

  @Test
  public void testTwo() {
    InterleavedInvocationTest.LOG.add("test2");
  }
}
