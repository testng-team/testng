package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeSuite;

class SuiteBBase {

  @BeforeSuite(dependsOnGroups = "childA")
  protected final void baseB() {}
}
