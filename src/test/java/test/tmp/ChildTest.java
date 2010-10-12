package test.tmp;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class ChildTest extends ParentTest {

  @Configuration(beforeTestMethod = true)
  public void btm2() {
    ppp("CHILD BEFORE TEST");
  }

  @Configuration(afterTestMethod = true)
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
