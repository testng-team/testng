package test.dependent;

import org.testng.annotations.Test;

public class GitHub261AlphaSample {

  @Test
  public void testAlpha1() {}

  @Test(dependsOnMethods = "testAlpha1")
  public void testAlpha2() {}
}
