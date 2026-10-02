package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;

class AlignedHardDepBase {

  @BeforeClass(groups = "setup")
  protected final void baseSetup() {}
}
