package test.dependent;

import org.testng.annotations.Test;

import test.BaseTest;


public class ClassDependsOnGroups extends BaseTest {
  @Test
  public void verifyDependsOnGroups() {
     addClass(test.dependent.DifferentClassDependsOnGroupsTest1.class.getName());
     addClass(test.dependent.DifferentClassDependsOnGroupsTest2.class.getName());

     run();
     String[] failed = {
        "test0"
     };
     String[] skipped = {
         "test2"
     };
     verifyTests("Failed", failed, getFailedTests());
     verifyTests("Skipped", skipped, getSkippedTests());
  }
}
