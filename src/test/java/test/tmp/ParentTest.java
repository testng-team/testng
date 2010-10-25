package test.tmp;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class ParentTest {

  @Configuration(beforeTestMethod = true)
  public void btm1() {
    ppp("PARENT BEFORE TEST");
  }

  @Configuration(afterTestMethod = true)
  public void atm1() {
    ppp("PARENT AFTER TEST");
  }

  @Test
  public void t1() {
    ppp("TEST PARENT");
  }

  private void ppp(String string) {
    System.out.println("[Parent] " + string);
  }



}
