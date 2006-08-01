package test.dependent;

import org.testng.annotations.Test;

import test.BaseTest;

public class MultiGroupTest extends BaseTest {
  @Test
  public void verifyDependsOnMultiGroups() {
     addClass("test.dependent.MultiGroup1SampleTest");
     addClass("test.dependent.MultiGroup2SampleTest");

     run();
     String[] passed = {
         "testA"
      };
     String[] failed = {
        "test1"
     };
     String[] skipped = {
         "test2"
     };
     verifyTests("Passed", passed, getPassedTests());
     verifyTests("Failed", failed, getFailedTests());
     verifyTests("Skipped", skipped, getSkippedTests());
  }
}
