package org.testng.dependent.samples.issue141;

import org.testng.annotations.Test;

public class ErrorScenarioNestedSample {

  @Test(
      dependsOnMethods =
          "org.testng.dependent.samples.issue141.ErrorScenarioNestedSample$InnerTestClass.rambo*")
  public void a() {}

  public static class InnerTestClass {
    @Test
    public void b() {}
  }
}
