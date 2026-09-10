package org.testng.reporters.spillfailure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;
import org.testng.reporters.issue1259.LargeReportSample;
import test.SimpleBaseTest;

/**
 * What a run does when no buffer can spill to disk.
 *
 * <p>Two things have to hold at once, and they pull in opposite directions. A buffer that could not
 * write must not hand back a document with a hole in it -- that is what this branch is for. And
 * {@code JUnitXMLReporter} is an {@code IResultListener2}, which {@code TestRunner} calls with
 * nothing around it, unlike every {@code IReporter}, which {@code TestNG.generateReports} wraps
 * individually. So raising from that listener ends the whole run and no report is written at all.
 *
 * <p>The child JVM is given a {@code java.io.tmpdir} that does not exist, which is the cheapest way
 * to make every spill fail at once. The workload is big enough that the JUnit XML crosses the size
 * at which a buffer spills -- around 300 KB for this many results.
 */
public class BrokenSpillTest extends SimpleBaseTest {

  private static final int ROWS = 3_000;

  private static final int TIMEOUT_MINUTES = 10;

  @Test(description = "A spill that cannot happen costs its report, not the run")
  public void aRunWhoseBuffersCannotSpillStillFinishes() throws Exception {
    File outputDirectory = createDirInTempDir("broken-spill");
    File log = new File(outputDirectory, "forked-output.log");

    int exitCode = run(outputDirectory, log);
    String output = new String(Files.readAllBytes(log.toPath()), StandardCharsets.UTF_8);

    assertThat(output)
        .as("the run did not finish%s", tail(output))
        .contains(BrokenSpillLauncher.MARKER);
    assertThat(exitCode).as("the run failed%s", tail(output)).isZero();
    // And it said so rather than losing the file in silence, which is what a logger nobody has
    // configured would have done.
    assertThat(output)
        .as("nothing said the JUnit XML report had been lost%s", tail(output))
        .contains("JUnit XML report for");
  }

  private static int run(File outputDirectory, File log) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
    // A directory that does not exist, so File.createTempFile fails for every buffer.
    command.add("-Djava.io.tmpdir=" + new File(outputDirectory, "absent").getAbsolutePath());
    command.add("-D" + LargeReportSample.ROWS_PROPERTY + "=" + ROWS);
    command.add("-Dfile.encoding=UTF-8");
    command.add("-Dstdout.encoding=UTF-8");
    command.add("-Dsun.stdout.encoding=UTF-8");
    command.add("-cp");
    command.add(System.getProperty("java.class.path"));
    command.add(BrokenSpillLauncher.class.getName());
    command.add(outputDirectory.getAbsolutePath());

    Process process =
        new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log).start();
    if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
      process.destroyForcibly().waitFor();
      throw new AssertionError(
          "The forked run did not finish within " + TIMEOUT_MINUTES + " minutes");
    }
    return process.exitValue();
  }

  private static String tail(String output) {
    return System.lineSeparator() + "--- forked output ---" + System.lineSeparator() + output;
  }
}
