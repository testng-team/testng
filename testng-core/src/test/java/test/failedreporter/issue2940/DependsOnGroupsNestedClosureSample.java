package test.failedreporter.issue2940;

import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;

public class DependsOnGroupsNestedClosureSample {
  public static final AtomicInteger remainingFailures = new AtomicInteger();

  @Test(groups = "prep")
  public void baseSetup() {}

  @Test
  public void setup() {}

  @Test(groups = "init", dependsOnMethods = "setup", dependsOnGroups = "prep")
  public void passedMember() {}

  @Test(groups = "init")
  public void failedMember() {
    if (remainingFailures.getAndDecrement() > 0) {
      fail();
    }
  }

  @Test(dependsOnGroups = "init.*")
  public void skippedDependent() {}
}
