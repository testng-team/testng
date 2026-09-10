package org.testng.methodinterceptors.samples;

import org.testng.annotations.Test;

public class DependsOnMethodsSample {

  @Test
  public void independent() {}

  @Test
  public void prerequisite() {}

  @Test(dependsOnMethods = "prerequisite")
  public void dependent() {}
}
