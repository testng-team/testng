package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeSuite;

class SuiteABase {

  @BeforeSuite(dependsOnGroups = "childB")
  protected final void baseA() {}
}
