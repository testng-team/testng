package org.testng.configuration.samples.issue3239;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

public class AfterSuiteBChild extends AfterSuiteBBase {

  @AfterSuite
  public void childB() {}

  @AfterSuite(groups = "childB", dependsOnMethods = "childB")
  public void childBGroup() {}

  @Test
  public void testB() {}
}
