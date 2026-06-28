package test.failedreporter.issue1297.inheritance;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class InheritanceFailureSample extends SampleBase {
  @Test
  public void newTest2() {
    fail();
  }
}
