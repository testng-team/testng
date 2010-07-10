package test.dataprovider;

import org.testng.annotations.Test;

import test.BaseTest;

public class ClassTest extends BaseTest {
  @Test(groups = { "current" })
  public void includeMethodsOnly() {
    addClass(ClassSampleTest.class.getName());
    run();
    String[] passed = {
      "f", "g"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }
}
