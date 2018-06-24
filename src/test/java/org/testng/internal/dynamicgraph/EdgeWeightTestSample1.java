package org.testng.internal.dynamicgraph;

import org.testng.annotations.Test;

public class EdgeWeightTestSample1 {

  @Test(
      groups = {"g1"},
      priority = 2)
  public void t1() {}

  @Test(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 0)
  public void t2() {}

  @Test(
      groups = {"g2"},
      dependsOnGroups = "g1",
      priority = 1)
  public void t3() {}
}
