package test.superclass;

import org.testng.annotations.Test;

@Test
public class Child2Test extends Base2 {
  public void t1() {
    ppp("T1");
  }

  public void t2() {
    ppp("T2");
  }

  public void t3() {
    ppp("T3");
  }

  private static void ppp(String s) {
    if (false) {
      System.out.println("[Child] " + s);
    }
  }
}
