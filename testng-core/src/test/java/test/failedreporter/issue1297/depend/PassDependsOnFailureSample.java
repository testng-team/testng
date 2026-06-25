package test.failedreporter.issue1297.depend;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class PassDependsOnFailureSample {
  @Test(dependsOnMethods = "newTest2")
  public void newTest1() {}

  @Test
  public void newTest2() {
    fail();
  }
}
