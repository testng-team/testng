package test.dependent;

import org.testng.annotations.Test;

import test.BaseTest;

public class MultiGroupTest extends BaseTest {
  @Test
  public void verifyDependsOnMultiGroups() {
     addClass(MultiGroup1SampleTest.class.getName());
     addClass(MultiGroup2SampleTest.class.getName());

     run();
     String[] passed = {
         "testA", "test2"
      };
     String[] failed = {
        "test1"
     };
     String[] skipped = {
     };
     verifyTests("Passed", passed, getPassedTests());
     verifyTests("Failed", failed, getFailedTests());
     verifyTests("Skipped", skipped, getSkippedTests());
  }
}
