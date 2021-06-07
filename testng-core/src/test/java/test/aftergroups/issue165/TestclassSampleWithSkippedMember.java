package test.aftergroups.issue165;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

public class TestclassSampleWithSkippedMember {

  @Test(groups = "A")
  public void a1() {
    throw new org.testng.SkipException("skip");
  }

  @Test(groups = "A", dependsOnMethods = "a1")
  public void a2() {}

  @AfterGroups(groups = "A")
  public void afterGroupsMethod() {}
}
