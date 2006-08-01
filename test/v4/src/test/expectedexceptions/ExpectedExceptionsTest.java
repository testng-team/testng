package test.expectedexceptions;

import org.testng.annotations.Test;

import test.BaseTest;

public class ExpectedExceptionsTest extends BaseTest {
  
  @Test
  public void expectedExceptions() {
    addClass("test.expectedexceptions.SampleExceptions");
    run();
    String[] passed = {
      "shouldPass",
    };
    String[] failed = {
        "shouldFail1", "shouldFail2"
    };
    String[] skipped = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

}


