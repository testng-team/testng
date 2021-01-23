package test.tmp;

import org.testng.annotations.BeforeTest;

public class Fixturable {

  private static void ppp(String s) {
    System.out.println("[Fixturable] " + s);
  }

  @BeforeTest(groups = "fixture")
  public void setupFixture() {
    ppp("SETUP");
  }
}
