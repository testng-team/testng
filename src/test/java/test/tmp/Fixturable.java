package test.tmp;

import org.testng.annotations.Configuration;

public class Fixturable {
  @Configuration(beforeTest=true, groups="fixture")
  public void setupFixture() {
    ppp("SETUP");
  }

  private static void ppp(String s) {
    System.out.println("[Fixturable] " + s);
  }
}
