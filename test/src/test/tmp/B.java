package test.tmp;

import org.testng.annotations.Test;

public class B {
  @Test
  public void f2() {
    ppp("f2 " + Thread.currentThread().getId());
  }
  
  private static void ppp(String s) {
    System.out.println("[FactoryTest] " + s);
  }
  
}
