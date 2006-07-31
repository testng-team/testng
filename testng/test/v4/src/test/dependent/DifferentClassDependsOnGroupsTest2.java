package test.dependent;


import org.testng.annotations.Test;


public class DifferentClassDependsOnGroupsTest2 {
  @Test(dependsOnGroups = { "mainGroup" })
  public void test1() {
  }
}
