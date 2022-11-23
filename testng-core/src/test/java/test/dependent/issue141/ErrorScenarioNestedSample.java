package test.dependent.issue141;

import org.testng.annotations.Test;

public class ErrorScenarioNestedSample {

  @Test(
      dependsOnMethods = "test.dependent.issue141.ErrorScenarioNestedSample$InnerTestClass.rambo*")
  public void a() {}

  public static class InnerTestClass {
    @Test
    public void b() {}
  }
}
