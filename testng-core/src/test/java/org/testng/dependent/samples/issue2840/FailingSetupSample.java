package org.testng.dependent.samples.issue2840;

import org.testng.Assert;
import org.testng.annotations.Test;

/** A failing method named like {@link BothSetupsSample#setup()}, which also depends on it. */
public class FailingSetupSample {

  @Test
  public void setup() {
    Assert.fail("setup failure");
  }
}
