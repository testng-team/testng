package test.tmp;

import org.testng.annotations.BeforeTest;

public class Fixturable {
  @BeforeTest(groups="fixture")
  public void setupFixture() {
    ppp("SETUP");
  }

  private static void ppp(String s) {
    System.out.println("[Fixturable] " + s);
  }
}
