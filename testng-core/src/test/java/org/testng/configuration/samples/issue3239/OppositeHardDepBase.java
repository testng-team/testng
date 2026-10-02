package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;

class OppositeHardDepBase {

  @BeforeClass(dependsOnGroups = "childFirst")
  protected final void baseSetup() {}
}
