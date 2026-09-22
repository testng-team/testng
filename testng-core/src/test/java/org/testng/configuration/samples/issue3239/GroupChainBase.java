package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;

class GroupChainBase {

  @BeforeClass(groups = "c")
  protected final void groupC() {}

  @BeforeClass(groups = "b", dependsOnGroups = "c")
  protected final void groupB() {}

  @BeforeClass(dependsOnGroups = "b")
  protected final void groupA() {}
}
