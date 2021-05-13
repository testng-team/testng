package test;

import org.testng.annotations.Test;

/**
 * This used to create a StackOverflowError.
 */
public class StaticTest {
  @Test
  public void test() {
  }

  @Test
  public static class InnerStaticClass extends StaticTest {
  }
}