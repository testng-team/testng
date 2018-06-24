package test.failedreporter.issue1297.inheritance;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InheritanceFailureSample extends SampleBase {
  @Test
  public void newTest2() {
    Assert.fail();
  }
}
