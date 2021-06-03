package test.tmp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ParentTest {

  @BeforeMethod
  public void btm1() {
    ppp("PARENT BEFORE TEST");
  }

  @AfterMethod
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
