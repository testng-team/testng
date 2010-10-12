package test.dependent;

import org.testng.annotations.Test;

import test.BaseTest;

public class MissingMethodTest extends BaseTest {

  @Test
  public void verifyThatExceptionIsThrownIfMissingMethod() {
    addClass("test.dependent.MissingMethodSampleTest");

    run();
    String[] passed = {
        "explicitlyIgnoreMissingMethod",
        "alwaysRunDespiteMissingMethod"
     };
    String[] failed = {
    };
    String[] skipped = {
    };
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());

  }

}
