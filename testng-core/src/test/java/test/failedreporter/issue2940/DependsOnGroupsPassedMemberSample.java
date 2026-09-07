package test.failedreporter.issue2940;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class DependsOnGroupsPassedMemberSample {
  @Test(groups = "init")
  public void passedMember() {}

  @Test(groups = "init")
  public void failedMember() {
    fail();
  }

  @Test(dependsOnGroups = "init")
  public void skippedDependent() {}
}
