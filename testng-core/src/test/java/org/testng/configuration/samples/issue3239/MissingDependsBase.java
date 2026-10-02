package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;

class MissingDependsBase {

  @BeforeClass(dependsOnMethods = "doesNotExist")
  protected final void baseSetup() {}
}
