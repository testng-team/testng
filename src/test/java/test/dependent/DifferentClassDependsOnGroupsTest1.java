package test.dependent;


import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;


public class DifferentClassDependsOnGroupsTest1 {
  @Test(groups = { "mainGroup" })
  public void test0() {
    assertTrue(1 == 0); // Force a failure
  }

  @Test(dependsOnGroups= {"mainGroup"})
  public void test2() {
  }
}
