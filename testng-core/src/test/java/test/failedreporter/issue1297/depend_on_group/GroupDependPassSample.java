package test.failedreporter.issue1297.depend_on_group;

import org.testng.annotations.Test;
import test.failedreporter.FailedReporterTest;

public class GroupDependPassSample extends GroupDependSampleBase {
  @Test(
      groups = FailedReporterTest.DEPENDENT_GROUP,
      dependsOnGroups = FailedReporterTest.DEPENDENCY_GROUP)
  public void newTest1() {}
}
