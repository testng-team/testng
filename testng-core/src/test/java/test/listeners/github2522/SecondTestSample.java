package test.listeners.github2522;

import org.testng.annotations.Test;

public class SecondTestSample {
  @Test(description = "First test step")
  public void firstMethod() {}

  @Test(description = "Second test step", dependsOnMethods = "firstMethod")
  public void secondMethod() {}

  @Test(description = "Third test step", dependsOnMethods = "secondMethod")
  public void thirdMethod() {}
}
