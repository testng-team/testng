package org.testng.internal.issue2195;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BaseClass {

  @BeforeMethod(
      alwaysRun = true,
      dependsOnMethods = {"dummyDependsOnMethod"})
  public void dummyMethod() {}

  @BeforeMethod
  public void dummyDependsOnMethod() {}

  @Test
  public void dummyTest() {}
}
