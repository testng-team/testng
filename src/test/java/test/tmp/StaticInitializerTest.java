package test.tmp;

import org.testng.annotations.Test;

@Test
public class StaticInitializerTest {

  static {
    foo();
  }

  private static void foo() {
    throw new RuntimeException("FAILING");
  }

  public void testMe() {
    System.err.println("**** testMe ****");
  }
}