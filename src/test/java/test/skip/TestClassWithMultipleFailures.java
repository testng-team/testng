package test.skip;

import org.testng.annotations.Test;

public class TestClassWithMultipleFailures {

  @Test
  public void father() {
    throw new RuntimeException("simulating a failure");
  }

  @Test
  public void mother() {
    throw new RuntimeException("simulating a failure");
  }

  @Test(dependsOnMethods = {"father", "mother"})
  public void child() {}
}
