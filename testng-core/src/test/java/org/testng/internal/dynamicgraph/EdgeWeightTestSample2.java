package org.testng.internal.dynamicgraph;

import org.testng.annotations.Test;

public class EdgeWeightTestSample2 {

  @Test(priority = 1)
  public void t1() {}

  @Test(
      groups = {"group2"},
      priority = 2)
  public void t2() {}

  @Test(
      groups = {"group3"},
      dependsOnGroups = "group2",
      priority = 3)
  public void t3() {}

  @Test(
      groups = {"group4"},
      dependsOnGroups = "group3",
      priority = 0)
  public void t4() {}

  @Test(
      groups = {"group4"},
      dependsOnGroups = "group3",
      priority = 1)
  public void t5() {}
}
