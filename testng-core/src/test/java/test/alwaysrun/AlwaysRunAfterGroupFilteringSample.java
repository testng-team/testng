package test.alwaysrun;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * GITHUB-526: the run selects only group {@code a}, so every after method here is in another group.
 */
public class AlwaysRunAfterGroupFilteringSample {

  @Test(groups = "a")
  public void testInGroupA() {}

  @AfterMethod(groups = "b", alwaysRun = true)
  public void alwaysRunAfterMethodInGroupB() {}

  @AfterMethod(groups = "b")
  public void afterMethodInGroupB() {}

  @AfterMethod(onlyForGroups = "b", alwaysRun = true)
  public void alwaysRunAfterMethodOnlyForGroupB() {}

  @AfterGroups(groups = "b", alwaysRun = true)
  public void alwaysRunAfterGroupB() {}
}
