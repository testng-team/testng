package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;

class ThreeLevelGrandParent {

  @BeforeClass(groups = "g")
  protected final void gpSetup() {}
}
