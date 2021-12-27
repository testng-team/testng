package test.configuration.issue2664.cls;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GroupDependenciesSample {

  @BeforeClass(groups = {"g1"})
  public void s3() {}

  @BeforeClass(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void s2() {}

  @BeforeClass(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void s1() {}

  @Test()
  public void test3() {}
}
