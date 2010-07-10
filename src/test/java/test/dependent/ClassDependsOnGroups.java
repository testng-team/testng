package test.dependent;

import org.testng.annotations.Test;

import test.BaseTest;


public class ClassDependsOnGroups extends BaseTest {
  @Test
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
     verifyTests("Failed", failed, getFailedTests());
     verifyTests("Skipped", skipped, getSkippedTests());
  }
}
