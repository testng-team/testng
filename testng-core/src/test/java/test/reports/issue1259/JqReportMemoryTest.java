package test.reports.issue1259;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

/**
 * What the default HTML report costs in memory, against what it writes.
 *
 * <p>GITHUB-1259 and GITHUB-2334 both end in an {@code OutOfMemoryError} thrown once every test of
 * the run had passed, from inside {@code org.testng.reporters.jq} -- the second one at {@code
 * NavigatorPanel.generateMethodList}, through {@code XMLStringBuffer.toXML()} and {@code
 * FileStringBuffer.toString()}. Those buffers spill to a temporary file past a size for exactly
 * this reason, and every panel then asked for the whole of one back as a {@code String}, which
 * costs it twice over: once to read the file, once more for the copy the regular expression of
 * {@code toXML()} makes.
 *
 * <p>The run happens in a child JVM because the heap is the subject: exhausting this one would take
 * the Gradle worker and the rest of the suite with it. {@link JqReportLauncher} is what it runs,
 * and says there why it registers {@code jq.Main} and no other reporter.
 *
 * <p>The numbers below were measured on both sides of the fix. At this workload the unmodified
 * reporter needed {@code -Xmx64m} and failed at {@code 32m}, {@code 48m} and {@code 56m}; the
 * streaming one needs {@code 28m}. {@code -Xmx48m} therefore fails whatever the run does and passes
 * with roughly a third of the heap to spare. What is below it is not this defect: at {@code 24m}
 * the child dies building {@code Model}, on the results the run retains, which is GITHUB-1979.
 */
public class JqReportMemoryTest extends SimpleBaseTest {

  private static final int ROWS = 20_000;

  /** See {@link JqReportLauncher#suiteName()} for why the length is the knob. */
  private static final int SUITE_NAME_LENGTH = 400;

  private static final String HEAP = "-Xmx48m";

  /**
   * Under the ~39 MB measured, but above the ~31 MB the same run writes without the suite-name knob
   * -- so a navigator that stopped naming the suite per method would fail here rather than quietly
   * leave this test measuring nothing.
   */
  private static final long SMALLEST_EXPECTED_REPORT = 35L * 1024 * 1024;

  private static final int TIMEOUT_MINUTES = 10;

  @Test(description = "GITHUB-1259, GITHUB-2334")
  public void aReportLargerThanTheHeapIsGeneratedWithoutExhaustingIt() throws Exception {
    File outputDirectory = createDirInTempDir("issue1259");
    try {
      Fork fork = run(outputDirectory);

      // The run itself is not what is being measured, so it has to have finished. Both issues
      // report their OutOfMemoryError under a summary saying every test passed.
      assertThat(fork.output)
          .as("the run itself did not finish, so nothing here is about the report")
          .contains("Total tests run: " + ROWS);
      assertThat(fork.output)
          .as("the report generation ran out of heap")
          .doesNotContain("OutOfMemoryError");
      assertThat(fork.output).contains("REPORT GENERATED");
      assertThat(fork.exitCode).isZero();

      // And the page really is one the heap could not have held a copy of.
      File page = new File(outputDirectory, "index.html");
      assertThat(page).exists();
      assertThat(page.length()).isGreaterThan(SMALLEST_EXPECTED_REPORT);
    } finally {
      deleteDir(outputDirectory);
    }
  }

  private static Fork run(File outputDirectory) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
    command.add(HEAP);
    command.add("-D" + LargeReportSample.ROWS_PROPERTY + "=" + ROWS);
    command.add("-D" + JqReportLauncher.SUITE_NAME_LENGTH_PROPERTY + "=" + SUITE_NAME_LENGTH);
    command.add("-cp");
    command.add(System.getProperty("java.class.path"));
    command.add(JqReportLauncher.class.getName());
    command.add(outputDirectory.getAbsolutePath());

    // Redirected to a file rather than drained here: reading the pipe to its end before waiting
    // makes the timeout cover only what is left after the child stopped writing, so a child that
    // went quiet and hung would be waited on for as long as it liked.
    File log = new File(outputDirectory, "forked-output.log");
    Process process =
        new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log).start();
    if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
      // Waited on, not just signalled: destroyForcibly returns before the child is gone, and the
      // caller deletes this directory on the way out -- with the child still holding the log file
      // open inside it, that deletion fails on Windows.
      process.destroyForcibly().waitFor();
      throw new AssertionError(
          "The forked report generation did not finish within " + TIMEOUT_MINUTES + " minutes");
    }
    return new Fork(
        process.exitValue(), new String(Files.readAllBytes(log.toPath()), StandardCharsets.UTF_8));
  }

  private static final class Fork {
    private final int exitCode;
    private final String output;

    private Fork(int exitCode, String output) {
      this.exitCode = exitCode;
      this.output = output;
    }
  }
}
