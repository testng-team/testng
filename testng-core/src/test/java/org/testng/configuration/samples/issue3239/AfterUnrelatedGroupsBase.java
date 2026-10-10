package org.testng.configuration.samples.issue3239;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

class AfterUnrelatedGroupsBase {

  @BeforeMethod(groups = "beforeMethod")
  public void beforeMethod() {}

  @AfterMethod(alwaysRun = true, dependsOnGroups = "beforeMethod")
  public void afterMethod() {}
}
