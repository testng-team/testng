package org.testng.dependent.samples;

import org.testng.annotations.Test;

public class DifferentClassDependsOnGroupsTest2 {
  @Test(dependsOnGroups = {"mainGroup"})
  public void test1() {}
}
