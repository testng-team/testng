package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class SuiteBChild extends SuiteBBase {

  @BeforeSuite
  public void childB() {}

  @BeforeSuite(groups = "childB", dependsOnMethods = "childB")
  public void childBGroup() {}

  @Test
  public void testB() {}
}
