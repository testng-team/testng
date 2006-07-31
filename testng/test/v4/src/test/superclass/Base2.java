package test.superclass;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test
public class Base2 {
  @Configuration(beforeTestClass = true)
  public void bc() {
    ppp("BEFORE_CLASS");
  }

  @Configuration(beforeTestMethod = true)
  public void bm() {
    ppp("BEFORE_METHOD");
  }

  public void tbase() {
    ppp("TEST IN BASE");
  }

  private static void ppp(String s) {
    if (false) {
      System.out.println("[Base] " + s);
    }
  }
}
