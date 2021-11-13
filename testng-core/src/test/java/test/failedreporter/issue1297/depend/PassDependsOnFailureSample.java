package test.failedreporter.issue1297.depend;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PassDependsOnFailureSample {
  @Test(dependsOnMethods = "newTest2")
  public void newTest1() {}

  @Test
  public void newTest2() {
    Assert.fail();
  }
}
