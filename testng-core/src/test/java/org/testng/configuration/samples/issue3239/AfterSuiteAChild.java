package org.testng.configuration.samples.issue3239;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

public class AfterSuiteAChild extends AfterSuiteABase {

  @AfterSuite
  public void childA() {}

  @AfterSuite(groups = "childA", dependsOnMethods = "childA")
  public void childAGroup() {}

  @Test
  public void testA() {}
}
