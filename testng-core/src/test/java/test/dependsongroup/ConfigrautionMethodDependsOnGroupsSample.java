package test.dependsongroup;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class ConfigrautionMethodDependsOnGroupsSample {

  @BeforeSuite(groups = {"g1"})
  public void beforesuite3() {}

  @BeforeSuite(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void beforesuite2() {}

  @BeforeSuite(
      groups = {"g2"},
      dependsOnGroups = "g1")
  public void beforesuite1() {}

  @Test
  public void test() {}
}
