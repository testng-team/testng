package test.methodinterceptors.issue1726;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class TestClassSample2 {

  @Priority(3)
  @Test(description = "Test Multiple Day Absence Request flow", groups = "Test")
  public void Test3() {
    Reporter.log(getClass().getName() + ".Test3", false);
  }

  @Priority(9)
  @Test(
      description = "Test Single Day Absence Request Flow",
      groups = {"Regression", "Test"})
  public void Test4() {
    Reporter.log(getClass().getName() + ".Test4", false);
  }
}
