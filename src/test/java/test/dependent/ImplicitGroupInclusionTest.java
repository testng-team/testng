package test.dependent;

import org.testng.annotations.Test;

import test.BaseTest;

public class ImplicitGroupInclusionTest extends BaseTest {

  @Test
  public void verifyImplicitGroupInclusion() {
    addClass("test.dependent.ImplicitGroupInclusionSampleTest");
    addIncludedGroup("b");

    run();
    String[] passed = {
        "a", "b", "z"
     };
    String[] failed = {
    };
    String[] skipped = {
    };
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void verifyImplicitGroupInclusion2() {
    addClass("test.dependent.ImplicitGroupInclusion2SampleTest");
    addIncludedGroup("g2");

    run();
    String[] passed = {
        "m3"
     };
    String[] failed = {
    };
    String[] skipped = {
    };
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void verifyImplicitGroupInclusion4() {
    addClass("test.dependent.ImplicitGroupInclusion4SampleTest");
    addIncludedGroup("g2");

    run();
    String[] passed = {
        "m3", "m4"
     };
    String[] failed = {
    };
    String[] skipped = {
    };
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void verifyImplicitGroupInclusion3() {
    addClass("test.dependent.ImplicitGroupInclusion3SampleTest");
    addIncludedGroup("inc");
    addExcludedGroup("exc");

    run();
    String[] passed = {
        "test1"
     };
    String[] failed = {
    };
    String[] skipped = {
    };
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void verifyImplicitMethodInclusion() {
    addClass("test.dependent.ImplicitMethodInclusionSampleTest");
    addIncludedGroup("windows");

    run();
    String[] passed = {
        "a", "b"
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
