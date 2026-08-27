package test.beforegroups.issue2804;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * GITHUB-2804: {@code dependsOnGroups} names a group by regular expression, and {@link #az1()} and
 * {@link #az2()} belong to both the group the configuration runs before and a group that expression
 * matches.
 */
public class GroupPatternDependencySample {

  @BeforeGroups(value = "A", dependsOnGroups = "Z.*")
  public void setUpA() {}

  @Test(
      groups = {"A", "Z1"},
      priority = 1)
  public void az1() {}

  @Test(
      groups = {"A", "Z1"},
      priority = 2)
  public void az2() {}

  @Test(groups = "Z2", priority = 3)
  public void z3() {}
}
