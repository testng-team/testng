package org.testng.internal.dynamicgraph;

import org.testng.annotations.Test;

public class SoftDependencyTestClassSample {
  @Test(priority = 1)
  public void a() {}

  @Test(priority = 2)
  public void b() {}
}
