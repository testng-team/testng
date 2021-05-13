package test.failedreporter.issue1297.groups;

import org.testng.Assert;
import org.testng.annotations.Test;

public class GroupsFailureSample extends GroupsSampleBase {
  @Test(groups = "run")
  public void newTest2() {
    Assert.fail();
  }
}
