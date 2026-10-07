package test.groups.issue2232;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;
import org.testng.annotations.Test;
import test.TestHelper;

/**
 * The command line half of {@code org.testng.groups.issue2232.IssueTest}, which stays in {@code
 * testng-core} for the in-process case. This test runs {@code org.testng.TestNG} in a child
 * process.
 */
public class IssueCommandLineTest {

  @Test(invocationCount = 2, description = "GITHUB-2232")
  // Ensuring that the bug doesn't surface even when tests are executed via the command line mode
  public void commandlineTest() throws IOException, InterruptedException {
    String suitefile = TestHelper.writeSuiteToTempFile(Issue2232Suites.construct());
    TestHelper.ForkResult result =
        TestHelper.runTestNG(Collections.emptyList(), Collections.singletonList(suitefile));
    assertThat(result.exitStatus)
        .as("CLI exit status, stdout:%n%s%nstderr:%n%s", result.stdout, result.stderr)
        .isEqualTo(0);
  }
}
