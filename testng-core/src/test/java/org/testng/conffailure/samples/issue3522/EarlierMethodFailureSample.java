package org.testng.conffailure.samples.issue3522;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EarlierMethodFailureSample {

  @BeforeMethod
  public void zSetup() {
    throw new RuntimeException("Z.zSetup fails");
  }

  @Test
  public void zTest() {}
}
