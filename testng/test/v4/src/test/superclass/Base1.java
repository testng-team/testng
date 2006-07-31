package test.superclass;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;
public class Base1 {
  @Configuration(beforeTestClass = true)
  public void bc() {
    ppp("BEFORE_CLASS");
  }

  @Configuration(beforeTestMethod = true)
  public void bm() {
    ppp("BEFORE_METHOD");
  }

  @Test
  public void tbase() {
    ppp("TEST IN BASE");
  }

  private static void ppp(String s) {
    if (false) {
      System.out.println("[Base] " + s);
    }
  }
}
