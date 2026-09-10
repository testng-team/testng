package org.testng.methodinterceptors.samples;

import org.testng.annotations.Test;

/** Group {@code a} and group {@code b} depend on each other, until {@code two} is dropped. */
public class CycleSample {

  @Test(groups = "a", dependsOnGroups = "b")
  public void one() {}

  @Test(groups = "b", dependsOnGroups = "a")
  public void two() {}

  @Test(groups = "b")
  public void twoPrime() {}
}
