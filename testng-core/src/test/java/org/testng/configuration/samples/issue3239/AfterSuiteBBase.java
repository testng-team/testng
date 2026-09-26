package org.testng.configuration.samples.issue3239;

import org.testng.annotations.AfterSuite;

class AfterSuiteBBase {

  @AfterSuite(dependsOnGroups = "childA")
  protected final void baseB() {}
}
