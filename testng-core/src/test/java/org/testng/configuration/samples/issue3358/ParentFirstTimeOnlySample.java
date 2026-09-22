package org.testng.configuration.samples.issue3358;

import org.testng.annotations.BeforeMethod;

public class ParentFirstTimeOnlySample {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeParent() {}
}
