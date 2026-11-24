package org.testng.dependent.samples.issue2840;

import org.testng.annotations.Test;

public class DownstreamSample {

  @Test(dependsOnMethods = "org.testng.dependent.samples.issue2840.UpstreamSample.passes")
  public void afterPassingUpstream() {}

  @Test(dependsOnMethods = "org.testng.dependent.samples.issue2840.UpstreamSample.fails")
  public void afterFailingUpstream() {}
}
