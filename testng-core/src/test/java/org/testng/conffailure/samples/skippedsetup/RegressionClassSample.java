package org.testng.conffailure.samples.skippedsetup;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Its @BeforeClass lists the failed group, so the failure check skips it. Its test is outside the
 * group but still needs the class setup.
 */
public class RegressionClassSample {

  @BeforeClass(
      alwaysRun = true,
      groups = {"smoke", "regression"})
  public void beforeClassB() {}

  @Test(groups = "regression")
  public void regressionTest() {}
}
