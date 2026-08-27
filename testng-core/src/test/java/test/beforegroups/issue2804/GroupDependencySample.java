package test.beforegroups.issue2804;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * GITHUB-2804 (and the sample contributed by PR #2025): the priorities make the natural ordering
 * prefer group {@code A} over group {@code Z}, so the only thing that can put {@code Z} first is
 * the dependency declared by {@link #setUpA()}.
 */
public class GroupDependencySample {

  @Test(groups = "A", priority = 1)
  public void a1() {}

  @Test(groups = "A", priority = 2)
  public void a2() {}

  @BeforeGroups(value = "A", dependsOnGroups = "Z")
  public void setUpA() {}

  @Test(groups = "Z", priority = 3)
  public void z1() {}

  @Test(groups = "Z", priority = 4)
  public void z2() {}
}
