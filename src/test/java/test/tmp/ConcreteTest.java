package test.tmp;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class ConcreteTest extends Fixturable {
  @Configuration(beforeTest=true, afterGroups="fixture")
  public void beforeFixture() {
    ppp("BEFORE");
  }

  @Test(groups = "fixture")
  public void test() {
    ppp("TEST");
  }


  private static void ppp(String s) {
    System.out.println("[ConcreteTest] " + s);
  }
}
