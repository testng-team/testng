package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GroupChainChild extends GroupChainBase {

  @BeforeClass
  public void childAgnostic() {}

  @BeforeClass(groups = "c", dependsOnMethods = "childAgnostic")
  public void childInC() {}

  @Test
  public void test() {}
}
