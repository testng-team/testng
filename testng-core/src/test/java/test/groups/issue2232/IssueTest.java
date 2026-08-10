package test.groups.issue2232;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {
  @Test(description = "GITHUB-2232", invocationCount = 2)
  // This test case doesn't vet out the fix completely because the bug by itself is very
  // sporadic and is not easy to reproduce. That is why this test is being executed 10 times
  // to ensure that the issue can be reproduced in one of the executions
  public void ensureNoNPEThrownWhenRunningGroups() throws InterruptedException {
    TestNG testng = create(Issue2232Suites.construct());
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
  }
}
