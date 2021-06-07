package test.skip;

import org.testng.annotations.Test;

public class TestClassWithMultipleGroupFailures {

  @Test(groups = "p1")
  public void father() {
    throw new RuntimeException("simulating a failure");
  }

  @Test(groups = "p2")
  public void mother() {
    throw new RuntimeException("simulating a failure");
  }

  @Test(
      groups = "all",
      dependsOnGroups = {"p1", "p2"})
  public void child() {}
}
