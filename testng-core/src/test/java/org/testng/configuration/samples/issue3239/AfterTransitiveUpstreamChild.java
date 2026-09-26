package org.testng.configuration.samples.issue3239;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class AfterTransitiveUpstreamChild extends AfterTransitiveUpstreamBase {

  @AfterClass(dependsOnGroups = "g")
  public void childDependsOnG() {}

  @AfterClass(dependsOnMethods = "childDependsOnG")
  public void childAgnostic() {}

  @Test
  public void test() {}
}
