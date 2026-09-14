package org.testng.dependent.samples.issue141;

import org.testng.annotations.Test;

public class SimpleSample {

  @Test(dependsOnMethods = "org.testng.dependent.samples.issue141.BSample.xx*")
  public void testMethod() {}
}
