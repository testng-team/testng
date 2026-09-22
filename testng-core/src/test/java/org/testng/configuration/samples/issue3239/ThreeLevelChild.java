package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ThreeLevelChild extends ThreeLevelParent {

  @BeforeClass
  public void childAgnostic() {}

  @BeforeClass(groups = "g", dependsOnMethods = "childAgnostic")
  public void childInG() {}

  @Test
  public void test() {}
}
