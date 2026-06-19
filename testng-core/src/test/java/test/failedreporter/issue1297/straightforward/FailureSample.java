package test.failedreporter.issue1297.straightforward;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FailureSample {
  @BeforeClass
  public void beforeClassFailureSample() {}

  @Test
  public void newTest2() {
    fail();
  }

  @AfterClass
  public void afterClassFailureSample() {}
}
