package test.dependent;

import org.testng.annotations.Test;

import test.BaseTest;

public class MissingGroupTest extends BaseTest {

  @Test
  public void verifyThatExceptionIsThrownIfMissingGroup() {
    addClass("test.dependent.MissingGroupSampleTest");

    run();
    String[] passed = {
        "shouldNotBeSkipped"
     };
    String[] failed = {
    };
    String[] skipped = {
      "shouldBeSkipped"
    };
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());

  }

}
