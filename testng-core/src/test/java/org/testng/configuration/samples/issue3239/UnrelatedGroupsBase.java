package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;

abstract class UnrelatedGroupsBase {

  @BeforeClass(alwaysRun = true, groups = "SomeTestGroup")
  protected final void zSetup() {}

  @BeforeClass(alwaysRun = true, dependsOnGroups = "SomeTestGroup")
  protected final void ySetup() {}
}
