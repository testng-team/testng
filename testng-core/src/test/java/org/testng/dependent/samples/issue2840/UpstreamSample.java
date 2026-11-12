package org.testng.dependent.samples.issue2840;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UpstreamSample {

  @Test
  public void passes() {}

  @Test
  public void fails() {
    Assert.fail("upstream failure");
  }
}
