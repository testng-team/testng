package org.testng.dependent.samples.issue2840;

import org.testng.annotations.Test;

public class BothSetupsSample {

  @Test
  public void setup() {}

  @Test(
      dependsOnMethods = {
        "org.testng.dependent.samples.issue2840.FailingSetupSample.setup",
        "setup"
      })
  public void run() {}
}
