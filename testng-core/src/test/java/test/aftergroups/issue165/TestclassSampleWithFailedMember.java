package test.aftergroups.issue165;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

public class TestclassSampleWithFailedMember {

  @Test(groups = "A")
  public void a1() {}

  @Test(groups = "A", dependsOnMethods = "a1")
  public void a2() {
    throw new org.testng.SkipException("skip");
  }

  @AfterGroups(groups = "A")
  public void afterGroupsMethod() {}
}
