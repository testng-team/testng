package test.failedreporter.issue1297.depend_on_group;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;
import test.failedreporter.FailedReporterTest;

public class GroupDependFailureSample extends GroupDependSampleBase {
  @Test(groups = FailedReporterTest.DEPENDENCY_GROUP)
  public void newTest2() {
    fail();
  }
}
