package test.failedreporter.issue1297.groups;

import org.testng.annotations.Test;

public class GroupsPassSample extends GroupsSampleBase {
  @Test(groups = "run")
  public void newTest1() {}
}
