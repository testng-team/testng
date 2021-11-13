package test.tmp;

import org.testng.annotations.BeforeTest;

public class Fixturable {
  @BeforeTest(groups = "fixture")
  public void setupFixture() {}
}
