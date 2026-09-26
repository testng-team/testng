package org.testng.configuration.samples.issue3239;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MissingGroupsChild extends MissingGroupsBase {

  @BeforeClass
  public void childAgnostic() {}

  @Test
  public void test() {}
}
