package org.testng.dependent;

import org.testng.annotations.Test;
import org.testng.dependent.samples.MultiGroup1SampleTest;
import org.testng.dependent.samples.MultiGroup2SampleTest;
import test.BaseTest;

public class MultiGroupTest extends BaseTest {
  @Test
  public void verifyDependsOnMultiGroups() {
    addClass(MultiGroup1SampleTest.class.getName());
    addClass(MultiGroup2SampleTest.class.getName());

    run();
    String[] passed = {
      "testA",
    };
    String[] failed = {"test1"};
    String[] skipped = {"test2"};
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }
}
