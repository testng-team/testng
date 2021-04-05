package test.junit;

import org.testng.annotations.Test;

public class SetUpExceptionTest extends test.BaseTest {

  @Test
  public void setUpFailingShouldCauseMethodsToBeSkipped() {
    addClass("test.junit.SetUpExceptionSampleTest");
    setJUnit(true);
    run();
    String[] passed = {
    };
    String[] failed = {
      "setUp"
    };
    String[] skipped = {
      "testM1", "tearDown"
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
    verifyTests("Failed", failed, getFailedTests());
  }
}
