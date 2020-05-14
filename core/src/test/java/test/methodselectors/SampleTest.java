package test.methodselectors;

import org.testng.annotations.Test;

public class SampleTest {

  @Test(groups = { "test1" })
  public void test1() {
    ppp("TEST1");

  }

  @Test(groups = { "test2" })
  public void test2() {
    ppp("TEST2");
  }

  @Test(groups = { "test3" })
  public void test3() {
    ppp("TEST3");
  }

  private static void ppp(String s) {
    if (false) {
      System.out.println("[SampleTest] " + s);
    }
  }
}
