package org.testng.dependent;

import org.testng.annotations.Test;
import test.BaseTest;

public class ClassDependsOnGroups extends BaseTest {
  @Test
  public void verifyDependsOnGroups() {
    addClass(org.testng.dependent.samples.DifferentClassDependsOnGroupsTest1.class.getName());
    addClass(org.testng.dependent.samples.DifferentClassDependsOnGroupsTest2.class.getName());

    run();
    String[] failed = {"test0"};
    String[] skipped = {"test1", "test2"};
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void verifyGroupsAcrossClasses() {
    addClass(org.testng.dependent.samples.C1.class.getName());
    addClass(org.testng.dependent.samples.C2.class.getName());

    run();
    String[] failed = {"failingTest"};
    String[] skipped = {"shouldBeSkipped"};
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }
}
