package org.testng.conffailure.samples.issue3522;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class InterfaceClassFailureSample implements IgnoredBeforeMethod {

  @BeforeClass
  public void aClass() {
    throw new RuntimeException("A.aClass fails");
  }

  @Test
  public void aTest() {}
}
