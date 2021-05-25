package test.tmp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChildTest extends ParentTest {

  @BeforeMethod
  public void btm2() {
    ppp("CHILD BEFORE TEST");
  }

  @AfterMethod
  public void atm2() {
    ppp("CHILD AFTER TEST");
  }

  @Override
  @Test
  public void t1() {
    ppp("TEST CHILD");
  }

  private void ppp(String string) {
    System.out.println("[Parent] " + string);
  }



}
