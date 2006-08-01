package test.dependent;

import test.BaseTest;

public class DependentAlwaysRunTest extends BaseTest {
  /**
   * @testng.test
   */
  public void verifyDependsOnMethodsAlwaysRun() {
    addClass("test.dependent.DependentOnMethod1AlwaysRunSampleTest");
  
    run();
    String[] passed = {
        "b", "verify"
     };
    String[] failed = {
       "a"
    };
    String[] skipped = {
    };
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }
  
  /**
   * @testng.test
   */
  public void verifyDependsOnGroups1AlwaysRun() {
    addClass("test.dependent.DependentOnGroup1AlwaysRunSampleTest");
  
    run();
    String[] passed = {
        "b", "verify"
     };
    String[] failed = {
       "a"
    };
    String[] skipped = {
    };
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  /**
   * @testng.test
   */
  public void verifyDependsOnGroups2AlwaysRun() {
    addClass("test.dependent.DependentOnGroup2AlwaysRunSampleTest");
  
    run();
    String[] passed = {
        "a2", "b", "verify"
     };
    String[] failed = {
       "a"
    };
    String[] skipped = {
    };
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

}
