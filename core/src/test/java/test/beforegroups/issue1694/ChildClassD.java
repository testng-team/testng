package test.beforegroups.issue1694;

import org.testng.annotations.Test;

public class ChildClassD extends BaseClassWithBeforeGroups {
  @Test(groups = "regression")
  public void testMethodD() {}
}
