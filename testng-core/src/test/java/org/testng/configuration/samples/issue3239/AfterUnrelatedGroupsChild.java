package org.testng.configuration.samples.issue3239;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AfterUnrelatedGroupsChild extends AfterUnrelatedGroupsBase {

  @BeforeMethod
  public void beforeChildMethod() {}

  @AfterMethod(groups = "unrelated")
  public void afterChildMethod() {}

  @Test
  public void testCase() {}
}
