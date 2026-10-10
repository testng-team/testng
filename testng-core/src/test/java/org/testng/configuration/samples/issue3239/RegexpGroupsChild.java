package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RegexpGroupsChild extends RegexpGroupsBase {

  @BeforeClass(groups = "setup-beta")
  public void childSetup() {}

  @Test
  public void test() {}
}
