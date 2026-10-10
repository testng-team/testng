package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;

class MissingGroupsBase {

  @BeforeClass(dependsOnGroups = "doesNotExist")
  protected final void baseSetup() {}
}
