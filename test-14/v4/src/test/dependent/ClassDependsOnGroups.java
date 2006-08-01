package test.dependent;

import test.BaseTest;


public class ClassDependsOnGroups extends BaseTest {
  /**
   * @testng.test
   */
  public void verifyDependsOnGroups() {
     addClass("test.dependent.DifferentClassDependsOnGroupsTest1");
     addClass("test.dependent.DifferentClassDependsOnGroupsTest2");

     run();
     String[] failed = {
        "test0"
     };
     String[] skipped = {
         "test1", "test2"
     };
     verifyTests("Passed", failed, getFailedTests());
     verifyTests("Skipped", skipped, getSkippedTests());
  }
}
