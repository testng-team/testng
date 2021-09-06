package test.failedreporter.issue1297.groups;

import org.testng.Assert;
import org.testng.annotations.Test;
import test.failedreporter.FailedReporterTest;

public class GroupsFailureSample extends GroupsSampleBase {
  @Test(groups = FailedReporterTest.DEPENDENT_GROUP)
  public void newTest2() {
    Assert.fail();
  }
}
