package test.failedreporter.issue1297.groups;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;
import test.failedreporter.FailedReporterTest;

public class GroupsFailureSample extends GroupsSampleBase {
  @Test(groups = FailedReporterTest.DEPENDENT_GROUP)
  public void newTest2() {
    fail();
  }
}
