package test.dependent.issue2658;

import org.testng.annotations.Test;

public class PassingClassSample extends BaseClassSample {
  @Test(dependsOnMethods = "test")
  public void passingMethod() {}
}
