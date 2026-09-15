package org.testng.conffailure.samples.issue3522;

import org.testng.annotations.BeforeMethod;

public class SharedConfigBase {

  @BeforeMethod(ignoreFailure = true)
  public void sharedSetup() {
    throw new RuntimeException("shared setup fails");
  }
}
