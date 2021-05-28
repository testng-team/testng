package test.beforegroups.issue1694;

import org.testng.annotations.Test;

public class ChildClassA extends BaseClassWithBeforeGroups {
  @Test(groups = "regression")
  public void testMethodA() {}
}
