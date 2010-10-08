package test.configuration;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class BeforeTestOrdering2Test extends BaseBeforeTestOrdering{

  @BeforeTest
  public void bt2() {
    log("bt2");
  }

  @AfterTest
  public void at2() {
    log("at2");
  }

  @Test
  public void f2() {
    log("f2");
  }

  private static void ppp(String s) {
    System.out.println("[BeforeTestOrdering2Test] " + s);
  }

}
