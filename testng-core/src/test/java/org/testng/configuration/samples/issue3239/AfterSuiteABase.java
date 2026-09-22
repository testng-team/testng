package org.testng.configuration.samples.issue3239;

import org.testng.annotations.AfterSuite;

class AfterSuiteABase {

  @AfterSuite(dependsOnGroups = "childB")
  protected final void baseA() {}
}
