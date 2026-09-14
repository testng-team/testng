package org.testng.dependent;

import org.testng.annotations.Test;
import test.BaseTest;

public class ImplicitGroupInclusionTest extends BaseTest {

  @Test
  public void verifyImplicitGroupInclusion() {
    addClass("org.testng.dependent.samples.ImplicitGroupInclusionSampleTest");
    addIncludedGroup("b");

    run();
    String[] passed = {"a", "b", "z"};
    String[] failed = {};
    String[] skipped = {};
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void verifyImplicitGroupInclusion2() {
    addClass("org.testng.dependent.samples.ImplicitGroupInclusion2SampleTest");
    addIncludedGroup("g2");

    run();
    String[] passed = {"m3"};
    String[] failed = {};
    String[] skipped = {};
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void verifyImplicitGroupInclusion4() {
    addClass("org.testng.dependent.samples.ImplicitGroupInclusion4SampleTest");
    addIncludedGroup("g2");

    run();
    String[] passed = {"m3", "m4"};
    String[] failed = {};
    String[] skipped = {};
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void verifyImplicitGroupInclusion3() {
    addClass("org.testng.dependent.samples.ImplicitGroupInclusion3SampleTest");
    addIncludedGroup("inc");
    addExcludedGroup("exc");

    run();
    String[] passed = {"test1"};
    String[] failed = {};
    String[] skipped = {};
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void verifyImplicitMethodInclusion() {
    addClass("org.testng.dependent.samples.ImplicitMethodInclusionSampleTest");
    addIncludedGroup("windows");

    run();
    String[] passed = {"a", "b"};
    String[] failed = {};
    String[] skipped = {};
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }
}
