package test.dependent.issue2658;

import org.testng.annotations.Test;

public class FailingClassSample extends BaseClassSample {
  @Test(dependsOnMethods = "test")
  void failingMethod() {}
}
