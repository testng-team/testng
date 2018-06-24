package test.dependent;

import org.testng.annotations.Test;

public class GitHub261BetaSample {

  @Test(dependsOnMethods = "testBeta2")
  public void testBeta1() {}

  @Test
  public void testBeta2() {}

  @Test(dependsOnMethods = "testBeta2")
  public void testBeta3() {}
}
