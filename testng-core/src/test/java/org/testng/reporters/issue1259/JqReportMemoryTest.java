package org.testng.reporters.issue1259;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.reporters.samples.issue1259.JqReportLauncher;
import org.testng.reporters.samples.issue1259.LargeReportSample;
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

  private static final Pattern LINES = Pattern.compile("\\R");

  @Test(description = "GITHUB-1259, GITHUB-2334")
  public void aReportLargerThanTheHeapIsGeneratedWithoutExhaustingIt() throws Exception {
    File outputDirectory = createDirInTempDir("issue1259");
    try {
      Fork fork = run(outputDirectory);

      // The run itself is not what is being measured, so it has to have finished. Both issues
      // report their OutOfMemoryError under a summary saying every test passed.
      assertThat(fork.output)
          .as("the run itself did not finish, so nothing here is about the report%s", fork.tail())
          .contains("Total tests run: " + ROWS);
      // Named rather than "an OutOfMemoryError happened": the Model this reporter builds first is
      // GITHUB-1979 and out of scope here, it also fails after the summary is printed, and it
      // would otherwise send a maintainer to the streaming panels for a defect that is not this
      // one.
      assertThat(outOfMemoryFrameIn(fork.output))
          .as("report generation ran out of heap%s", fork.tail())
          .isNull();
      assertThat(fork.output)
          .as("the report was not written%s", fork.tail())
          .contains("REPORT GENERATED");
      assertThat(fork.exitCode).as("the forked run failed%s", fork.tail()).isZero();

      // And the page really is one the heap could not have held a copy of.
      File page = new File(outputDirectory, "index.html");
      assertThat(page).exists();
      assertThat(page.length()).isGreaterThan(SMALLEST_EXPECTED_REPORT);
    } finally {
      // Best effort: a directory that will not delete -- Windows still holding the 39 MB page --
      // must not replace the failure above with an IOException about a temporary directory.
      try {
        deleteDir(outputDirectory);
      } catch (Exception cleanup) {
        Reporter.log("Could not delete " + outputDirectory + ": " + cleanup, true);
      }
    }
  }

  @Test(description = "The frame reported is the one that ran out of heap, not merely the fact")
  public void theOutOfMemoryFrameNamesWhereTheChildDied() {
    // What this distinguishes: the Model this reporter builds first is GITHUB-1979 and out of
    // scope here, and it fails after the summary is printed, so "an OutOfMemoryError happened"
    // sent a maintainer to the streaming panels for a defect that is not this one.
    // Both recorded from a real child at -Xmx24m. Which frame allocated last varies between runs,
    // and only one of the two goes through org.testng.internal.reporters at all.
    String modelViaStringBuilder =
        String.join(
            System.lineSeparator(),
            "Total tests run: 20000",
            "Exception in thread \"main\" java.lang.OutOfMemoryError: Java heap space",
            "\tat java.base/java.lang.StringBuilder.toString(StringBuilder.java:478)",
            "\tat org.testng.reporters.jq.Model.getTestResultName(Model.java:176)",
            "\tat org.testng.reporters.jq.Model.init(Model.java:63)");
    String modelViaSnapshots =
        String.join(
            System.lineSeparator(),
            "Total tests run: 20000",
            "Exception in thread \"main\" java.lang.OutOfMemoryError: Java heap space",
            "\tat java.base/java.util.stream.ReferencePipeline.map(ReferencePipeline.java:207)",
            "\tat org.testng.internal.reporters.ParameterSnapshot.plainValues(ParameterSnapshot.java:105)",
            "\tat org.testng.internal.reporters.ParameterSnapshots.reportedPlainValuesOf(ParameterSnapshots.java:270)",
            "\tat org.testng.reporters.jq.Model.getTestResultName(Model.java:167)",
            "\tat org.testng.reporters.jq.Model.init(Model.java:63)");
    String panelFailure =
        String.join(
            System.lineSeparator(),
            "Exception in thread \"main\" java.lang.OutOfMemoryError: Java heap space",
            "\tat org.testng.reporters.FileStringBuffer.toString(FileStringBuffer.java:140)",
            "\tat org.testng.reporters.jq.NavigatorPanel.generateMethodList(NavigatorPanel.java:286)");

    // Both name Model, which is the word that ties the failure to GITHUB-1979 rather than here.
    assertThat(outOfMemoryFrameIn(modelViaStringBuilder))
        .isEqualTo("at org.testng.reporters.jq.Model.getTestResultName(Model.java:176)");
    assertThat(outOfMemoryFrameIn(modelViaSnapshots))
        .isEqualTo("at org.testng.reporters.jq.Model.getTestResultName(Model.java:167)");
    assertThat(outOfMemoryFrameIn(panelFailure))
        .isEqualTo("at org.testng.reporters.FileStringBuffer.toString(FileStringBuffer.java:140)");
    // A run that finished says nothing, which is what the assertion above reads as a pass.
    assertThat(
            outOfMemoryFrameIn(
                "Total tests run: 20000" + System.lineSeparator() + "REPORT GENERATED"))
        .isNull();
  }

  @Test(description = "A log the child never wrote reads as empty rather than failing the report")
  public void anAbsentChildLogIsNotAnError() throws IOException {
    // The timeout path reads the log to say where the child stopped; a child that died before
    // creating it must not replace "did not finish within 10 minutes" with a FileNotFoundException.
    File missing = new File(createDirInTempDir("issue1259-absent"), "forked-output.log");

    assertThat(read(missing)).isEmpty();
  }

  private static Fork run(File outputDirectory) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
    command.add(HEAP);
    command.add("-D" + LargeReportSample.ROWS_PROPERTY + "=" + ROWS);
    command.add("-D" + JqReportLauncher.SUITE_NAME_LENGTH_PROPERTY + "=" + SUITE_NAME_LENGTH);
    // Explicit, so the log this test reads back does not depend on the child's platform default.
    // Three spellings because no one of them covers every JDK the matrix runs: file.encoding drove
    // System.out until 18, stdout.encoding exists from 19, and sun.stdout.encoding bridges them.
    command.add("-Dfile.encoding=UTF-8");
    command.add("-Dstdout.encoding=UTF-8");
    command.add("-Dsun.stdout.encoding=UTF-8");
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
          "The forked report generation did not finish within "
              + TIMEOUT_MINUTES
              + " minutes"
              + tailOf(read(log)));
    }
    return new Fork(process.exitValue(), read(log));
  }

  /**
   * @return the child's output, decoded as it was written -- the child is given an explicit UTF-8
   *     so this does not depend on the platform default charset on either side.
   */
  private static String read(File log) throws IOException {
    if (!log.isFile()) {
      return "";
    }
    return new String(Files.readAllBytes(log.toPath()), StandardCharsets.UTF_8);
  }

  /**
   * @return the first {@code OutOfMemoryError} frame under {@code org.testng.reporters}, or null if
   *     the child did not run out of heap.
   *     <p>Not {@code org.testng}: which frame allocated last is not stable -- the same run dies at
   *     {@code StringBuilder.toString} or inside {@code org.testng.internal.reporters} depending on
   *     what tips the heap -- and neither of those names a reporter. Skipping to {@code
   *     org.testng.reporters} answers the model or the panel, whichever it was.
   */
  private static @Nullable String outOfMemoryFrameIn(String output) {
    int oom = output.indexOf("OutOfMemoryError");
    if (oom < 0) {
      return null;
    }
    return LINES
        .splitAsStream(output.substring(oom))
        .map(String::trim)
        .filter(line -> line.startsWith("at org.testng.reporters."))
        .findFirst()
        .orElse("OutOfMemoryError with no org.testng.reporters frame");
  }

  /** The child's output, for a failure message: it is the only record of where it died. */
  private static String tailOf(String output) {
    return System.lineSeparator() + "--- forked output ---" + System.lineSeparator() + output;
  }

  private static final class Fork {
    private final int exitCode;
    private final String output;

    String tail() {
      return tailOf(output);
    }

    private Fork(int exitCode, String output) {
      this.exitCode = exitCode;
      this.output = output;
    }
  }
}
