package org.testng.dependent.samples.issue141;

import org.testng.annotations.Test;

public class ASample {
  @Test(dependsOnMethods = "org.testng.dependent.samples.issue141.BSample.b*")
  public void a() {}
}
