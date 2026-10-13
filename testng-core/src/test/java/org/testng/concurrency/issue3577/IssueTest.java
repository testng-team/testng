package org.testng.concurrency.issue3577;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;
import org.testng.concurrency.samples.issue3577.ParallelLogLauncher;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  private static final int TIMEOUT_MINUTES = 2;

  /**
   * The line goes to the INFO level of the logger of {@code Utils}. The test classpath sets the
   * default level of slf4j-simple to WARN. slf4j-simple reads the level of a logger only once, when
   * it creates the logger. So the run happens in a child Java virtual machine (JVM). There, a
   * system property sets that one logger to INFO.
   */
  @Test(description = "GITHUB-3577")
  public void theExecutorLogLineShowsTheNameOfTheTest() throws Exception {
    File outputDirectory = createDirInTempDir("issue3577");
    File log = new File(outputDirectory, "forked-output.log");

    int exitCode = run(outputDirectory, log);
    String output = new String(Files.readAllBytes(log.toPath()), StandardCharsets.UTF_8);

    assertThat(exitCode).as("the run failed%s", tail(output)).isZero();
    assertThat(output)
        .as("the executor log line%s", tail(output))
        .contains(
            "[TestTaskExecutor] Starting executor test "
                + ParallelLogLauncher.TEST_NAME
                + " with time out: ");
  }

  private static int run(File outputDirectory, File log) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
    command.add("-Dorg.slf4j.simpleLogger.log.org.testng.internal.Utils=info");
    command.add("-cp");
    command.add(System.getProperty("java.class.path"));
    command.add(ParallelLogLauncher.class.getName());
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
