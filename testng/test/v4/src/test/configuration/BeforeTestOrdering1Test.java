package test.configuration;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class BeforeTestOrdering1Test extends BaseBeforeTestOrdering {
  
  @Configuration(beforeTest = true)
  public void bt1() {
    log("bt1");
  }
  
  @Configuration(afterTest = true)
  public void at1() {
    log("at1");
  }
  
  @Test
  public void f1() {
    log("f1");
  }

  private static void ppp(String s) {
    System.out.println("[BeforeTestOrdering1Test] " + s);
  }

}
