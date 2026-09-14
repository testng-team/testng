package org.testng.dependent;

import org.testng.annotations.Test;
import test.BaseTest;

public class DependentAlwaysRunTest extends BaseTest {
  @Test
  public void verifyDependsOnMethodsAlwaysRun() {
    addClass("org.testng.dependent.samples.DependentOnMethod1AlwaysRunSampleTest");

    run();
    String[] passed = {"b", "verify"};
    String[] failed = {"a"};
    String[] skipped = {};
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void verifyDependsOnGroups1AlwaysRun() {
    addClass("org.testng.dependent.samples.DependentOnGroup1AlwaysRunSampleTest");

    run();
    String[] passed = {"b", "verify"};
    String[] failed = {"a"};
    String[] skipped = {};
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void verifyDependsOnGroups2AlwaysRun() {
    addClass("org.testng.dependent.samples.DependentOnGroup2AlwaysRunSampleTest");

    run();
    String[] passed = {"a2", "b", "verify"};
    String[] failed = {"a"};
    String[] skipped = {};
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }
}
