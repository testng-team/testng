package test.configuration.issue2663;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/** Group level configuration methods of one class, ordered by priority against each other. */
public class GroupsConfigSample {

  @BeforeGroups(groups = "grp", priority = 2)
  public void alphaBeforeGroups() {}

  @BeforeGroups(groups = "grp", priority = 1)
  public void bravoBeforeGroups() {}

  @AfterGroups(groups = "grp", priority = 2)
  public void alphaAfterGroups() {}

  @AfterGroups(groups = "grp", priority = 1)
  public void bravoAfterGroups() {}

  @Test(groups = "grp")
  public void test() {}
}
