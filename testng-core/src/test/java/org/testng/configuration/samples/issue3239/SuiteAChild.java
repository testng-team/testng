package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class SuiteAChild extends SuiteABase {

  @BeforeSuite
  public void childA() {}

  @BeforeSuite(groups = "childA", dependsOnMethods = "childA")
  public void childAGroup() {}

  @Test
  public void testA() {}
}
