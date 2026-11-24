package org.testng.dependent.samples.issue2840;

import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactorySample {

  private final boolean fail;

  public FactorySample(boolean fail) {
    this.fail = fail;
  }

  @Factory
  public static Object[] instances() {
    return new Object[] {new FactorySample(true), new FactorySample(false)};
  }

  @Test
  public void first() {
    Assert.assertFalse(fail, "failing instance");
  }

  @Test(dependsOnMethods = "first")
  public void second() {}
}
