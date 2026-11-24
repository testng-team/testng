package org.testng.conffailure.samples.issue3616;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SkippingSetupSample {

  @BeforeClass
  public void setup() {
    throw new SkipException("SkippingSetupSample setup skipped");
  }

  @Test
  public void test() {}
}
