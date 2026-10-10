package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UnrelatedGroupsChild extends UnrelatedGroupsBase {

  @BeforeClass(groups = "unrelated")
  public void thisSetup() {}

  @Test
  public void test() {}
}
