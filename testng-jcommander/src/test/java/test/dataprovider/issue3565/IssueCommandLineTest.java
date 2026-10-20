package test.dataprovider.issue3565;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.cli.jcommander.JCommanderCliRunner;
import org.testng.testhelper.OutputDirectoryPatch;
import test.SimpleBaseTest;

/**
 * GITHUB-3565. The command line reports a data provider failure the way the Java API does: as a
 * skip, unless {@code -propagateDataProviderFailureAsTestFailure} is given.
 */
public class IssueCommandLineTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3565")
  public void commandLineReportsADataProviderFailureAsASkipByDefault() {
    TestListenerAdapter results = runFromCommandLine();
    assertThat(results.getFailedTests()).isEmpty();
    assertThat(results.getSkippedTests()).hasSize(1);
  }

  @Test(description = "GITHUB-3565")
  public void commandLineReportsADataProviderFailureAsAFailureWhenAsked() {
    TestListenerAdapter results = runFromCommandLine("-propagateDataProviderFailureAsTestFailure");
    assertThat(results.getFailedTests()).hasSize(1);
    assertThat(results.getSkippedTests()).isEmpty();
  }

  @Test(description = "GITHUB-3565")
  public void javaApiAgreesWithTheCommandLineDefault() {
    TestListenerAdapter results = new TestListenerAdapter();
    TestNG testng = create(BrokenDataProviderSample.class);
    testng.addListener(results);
    testng.run();
    assertThat(results.getFailedTests()).isEmpty();
    assertThat(results.getSkippedTests()).hasSize(1);
  }

  private static TestListenerAdapter runFromCommandLine(String... extra) {
    String[] base = {
      "-log",
      "0",
      "-d",
      OutputDirectoryPatch.getOutputDirectory(),
      "-testclass",
      BrokenDataProviderSample.class.getCanonicalName()
    };
    String[] argv = new String[base.length + extra.length];
    System.arraycopy(base, 0, argv, 0, base.length);
    System.arraycopy(extra, 0, argv, base.length, extra.length);
    TestListenerAdapter results = new TestListenerAdapter();
    new JCommanderCliRunner().run(argv, results);
    return results;
  }
}
