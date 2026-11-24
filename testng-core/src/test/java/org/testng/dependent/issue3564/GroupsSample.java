package org.testng.dependent.issue3564;

import org.testng.annotations.Test;

public class GroupsSample {

  @Test(groups = "g1")
  public void inOneGroup() {}

  @Test(groups = {"g1", "g2"})
  public void inTwoGroups() {}

  @Test
  public void inNoGroup() {}
}
