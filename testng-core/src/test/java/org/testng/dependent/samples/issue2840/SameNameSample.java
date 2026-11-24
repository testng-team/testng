package org.testng.dependent.samples.issue2840;

import org.testng.annotations.Test;

public class SameNameSample {

  @Test
  public void first() {}

  @Test(dependsOnMethods = "first")
  public void second() {}
}
