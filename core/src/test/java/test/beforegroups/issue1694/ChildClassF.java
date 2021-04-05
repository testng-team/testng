package test.beforegroups.issue1694;

import org.testng.annotations.Test;

public class ChildClassF extends BaseClassWithBeforeGroups {
  @Test(groups = "regression")
  public void testMethodF() {}
}
