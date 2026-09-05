package test.groups.issue2232;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.testng.Reporter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.TestHelper;

/**
 * The command line half of {@code org.testng.groups.issue2232.IssueTest}, which stays in {@code
 * testng-core} for the in-process case. This is the only test in the build that runs {@code
 * org.testng.TestNG} as a real forked process.
 */
public class IssueCommandLineTest {

  private static final int FORK_TIMEOUT_MINUTES = 5;

  @Test(invocationCount = 2, description = "GITHUB-2232")
  // Ensuring that the bug doesn't surface even when tests are executed via the command line mode
  public void commandlineTest() throws IOException, InterruptedException {
    String suitefile = TestHelper.writeSuiteToTempFile(Issue2232Suites.construct());
    List<String> args = Collections.singletonList(suitefile);
    int status = exec(Collections.emptyList(), args);
    assertThat(status).isEqualTo(0);
  }

  private int exec(List<String> jvmArgs, List<String> args)
      throws IOException, InterruptedException {

    String javaHome = System.getProperty("java.home");
    String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
    String classpath = System.getProperty("java.class.path");
    String className = TestNG.class.getName();
    List<String> command = new ArrayList<>();
    command.add(javaBin);
    command.addAll(jvmArgs);
    command.add("-cp");
    command.add(classpath);
    command.add(className);
    command.addAll(args);
    Reporter.log("Executing the command " + command, 2, true);
    ProcessBuilder builder = new ProcessBuilder(command);
    Process process = builder.inheritIO().start();
    if (!process.waitFor(FORK_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
      process.destroyForcibly();
      throw new AssertionError(
          "The forked TestNG run did not finish within " + FORK_TIMEOUT_MINUTES + " minutes");
    }

    return process.exitValue();
  }
}
