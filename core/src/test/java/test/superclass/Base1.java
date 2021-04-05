package test.superclass;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
public class Base1 {
  @BeforeClass
  public void bc() {
    ppp("BEFORE_CLASS");
  }

  @BeforeMethod
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
