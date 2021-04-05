package test.multiple;

import org.testng.annotations.Test;

import test.BaseTest;

public class TestMultiple extends BaseTest {

  private static final String CLASS_NAME = "test.multiple.ThisFactory";

  @Test(groups = { "current" })
  public void multiple() {
    addClass(CLASS_NAME);
    run();
    String[] passed = {
      "f1",
    };
    String[] failed = {
      "f1","f1","f1","f1","f1", "f1","f1","f1","f1",
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

}
