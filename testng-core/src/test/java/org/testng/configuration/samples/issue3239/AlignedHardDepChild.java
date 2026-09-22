package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AlignedHardDepChild extends AlignedHardDepBase {

  @BeforeClass(dependsOnGroups = "setup")
  public void childSetup() {}

  @Test
  public void test() {}
}
