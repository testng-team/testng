package org.testng.cli.jcommander;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.TestHelper;

public class MissingJCommanderTest {

  private static final int TIMEOUT_MINUTES = 2;

  /**
   * The command line runner of TestNG is on the class path, but jcommander is not. The runner fails
   * to load, and the message must say so. A child Java virtual machine (JVM) runs the class path
   * without jcommander, because {@code TestNG.main} stops the JVM.
   */
  @Test(description = "GITHUB-3570")
  public void theMessageNamesTheClassThatIsMissing() throws Exception {
    File outputDirectory = TestHelper.createRandomDirectory().toFile();
    File log = new File(outputDirectory, "forked-output.log");

    int exitCode = run(log);
    String output = new String(Files.readAllBytes(log.toPath()), StandardCharsets.UTF_8);

    assertThat(exitCode).as("the exit code%s", tail(output)).isEqualTo(1);
    assertThat(output)
        .as("the forked output%s", tail(output))
        .doesNotContain("Exception in thread")
        .contains("Caused by:")
        .contains("NoClassDefFoundError")
        .contains("com/beust/jcommander/ParameterException");
  }

  @SuppressWarnings("deprecation")
  private static int run(File log) throws IOException, InterruptedException {
    List<String> classPath = new ArrayList<>();
    for (String entry : System.getProperty("java.class.path").split(File.pathSeparator, -1)) {
      if (!new File(entry).getName().toLowerCase(Locale.ROOT).startsWith("jcommander-")) {
        classPath.add(entry);
      }
    }
    List<String> command = new ArrayList<>();
    command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
    command.add("-cp");
    command.add(String.join(File.pathSeparator, classPath));
    command.add(TestNG.class.getName());
    command.add("testng.xml");

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
