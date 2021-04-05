package test.beforegroups.issue1694;

import org.testng.annotations.Test;

public class ChildClassC extends BaseClassWithBeforeGroups {
  @Test(groups = "regression")
  public void testMethodC() {}
}
