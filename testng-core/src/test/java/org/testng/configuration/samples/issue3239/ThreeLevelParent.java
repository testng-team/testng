package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;

class ThreeLevelParent extends ThreeLevelGrandParent {

  @BeforeClass(dependsOnGroups = "g")
  protected final void parentSetup() {}
}
