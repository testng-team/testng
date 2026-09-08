package org.testng.skip.samples;

import org.testng.annotations.Test;

public class TestClassWithFailedMethodInParentClass extends TestClassWithFailedMethod {

  @Test(dependsOnMethods = "parentMethod")
  public void anotherChild() {}
}
