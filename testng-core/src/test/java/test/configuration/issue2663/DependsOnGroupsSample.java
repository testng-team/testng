package test.configuration.issue2663;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** The same shape as {@link DependsOnMethodsSample} expressed with a group dependency. */
public class DependsOnGroupsSample {

  @BeforeMethod(groups = "init", priority = 5)
  public void charlieBefore() {}

  @BeforeMethod(dependsOnGroups = "init", priority = 5)
  public void alphaBefore() {}

  @BeforeMethod(priority = 1)
  public void bravoBefore() {}

  @Test
  public void test() {}
}
