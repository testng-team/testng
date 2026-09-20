package org.testng.conffailure.samples.issue3522;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ClassFailureWithIgnoredMethodSample {

  @BeforeClass
  public void aClass() {
    throw new RuntimeException("A.aClass fails");
  }

  @BeforeMethod(ignoreFailure = true)
  public void aSetup() {}

  @Test
  public void aTest() {}
}
