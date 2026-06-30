package test.listeners.github2522;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class FirstTestSample {
  @Test(description = "First test step")
  public void firstMethod() {}

  @Test(description = "Second test step", dependsOnMethods = "firstMethod")
  public void secondMethod() {
    fail();
  }

  @Test(description = "Third test step", dependsOnMethods = "secondMethod")
  public void thirdMethod() {}
}
