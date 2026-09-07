package test.skip;

import org.testng.annotations.Test;

public class TestClassWithGroupFailures {

  @Test(groups = "unit")
  public void unitTests() {
    throw new RuntimeException("simulating a failure");
  }

  @Test(groups = "integration", dependsOnGroups = "unit")
  public void integrationTests() {}
}
