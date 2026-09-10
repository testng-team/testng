package org.testng.dependent.samples.issue2658;

import org.testng.annotations.Test;

public class PassingClassSample extends BaseClassSample {
  @Test(dependsOnMethods = "test")
  public void passingMethod() {}
}
