package org.testng.conffailure.samples.skippedsetup;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Its @BeforeClass lists the failed group, so the failure check skips it. That skip must not turn
 * into a class failure that reaches a test outside the group.
 */
public class RegressionClassSample {

  @BeforeClass(
      alwaysRun = true,
      groups = {"smoke", "regression"})
  public void beforeClassB() {}

  @Test(groups = "regression")
  public void regressionTest() {}
}
