package test.superclass;

import org.testng.annotations.Test;

import test.BaseTest;

public class Test3 extends BaseTest {

  @Test
  public void shouldExcludeBaseMethods() {
    addClass("test.superclass.ChildSampleTest3");
    addExcludedMethod("test.superclass.ChildSampleTest3", "pass");
    addExcludedMethod("test.superclass.ChildSampleTest3", "base");
    run();
    String[] passed = {
    };
    String[] failed = {
        "fail"
    };
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
  }
}
