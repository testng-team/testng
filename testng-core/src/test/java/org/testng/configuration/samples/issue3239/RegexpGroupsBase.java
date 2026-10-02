package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;

class RegexpGroupsBase {

  @BeforeClass(groups = "setup-alpha")
  protected final void baseSetup() {}

  @BeforeClass(dependsOnGroups = "setup-.*")
  protected final void baseAfter() {}
}
