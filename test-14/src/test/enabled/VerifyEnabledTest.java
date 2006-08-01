package test.enabled;

import test.BaseTest;

public class VerifyEnabledTest extends BaseTest {
  
  /**
   * @testng.test
   */
  public void disabledMethodsShouldNotRun() {
    addClass("test.enabled.EnabledTest");
  
    run();
    String[] failed = {
    };
    String[] passed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

}
