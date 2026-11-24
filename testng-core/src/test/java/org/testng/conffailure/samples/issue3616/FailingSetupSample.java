package org.testng.conffailure.samples.issue3616;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FailingSetupSample {

  @BeforeClass
  public void setup() {
    throw new IllegalStateException("FailingSetupSample setup failed");
  }

  @Test
  public void test() {}
}
