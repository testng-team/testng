package test.failedreporter.issue1297.straightforward;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FailureSample {
  @BeforeClass
  public void beforeClassFailureSample() {}

  @Test
  public void newTest2() {
    Assert.fail();
  }

  @AfterClass
  public void afterClassFailureSample() {}
}
