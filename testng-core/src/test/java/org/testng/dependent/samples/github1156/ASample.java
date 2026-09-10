package org.testng.dependent.samples.github1156;

import org.testng.annotations.Test;

public class ASample {

  @Test(dependsOnMethods = "org.testng.dependent.samples.github1156.BSample.testB")
  public void testA() {}
}
