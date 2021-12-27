package test.configuration.issue2664.test;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class GroupDependenciesChildSample extends GroupDependenciesBaseClass {

  @BeforeTest(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void s2() {}

  @BeforeTest(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void s1() {}

  @Test()
  public void test3() {}
}
