package test.configuration.issue2664.suite;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class GroupDependenciesChildSample extends GroupDependenciesBaseClass {

  @BeforeSuite(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void s2() {}

  @BeforeSuite(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void s1() {}

  @Test()
  public void test3() {}
}
