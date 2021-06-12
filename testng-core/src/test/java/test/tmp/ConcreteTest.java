package test.tmp;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ConcreteTest extends Fixturable {
  @BeforeTest
  @AfterGroups("fixture")
  public void beforeFixture() {}

  @Test(groups = "fixture")
  public void test() {}
}
