package org.testng.conffailure.samples.skippedsetup;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/** Its @BeforeGroups is skipped because its @BeforeClass failed; group g must not be marked. */
public class GroupSetupOwnerSample {

  @BeforeClass
  public void beforeClassA() {
    throw new IllegalStateException("A.beforeClass fails");
  }

  @BeforeGroups(value = "g", alwaysRun = true)
  public void beforeGroupsG() {}

  @Test(groups = "g")
  public void testA() {}
}
