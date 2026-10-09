package org.testng.cli.jcommander;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.IntSupplier;
import org.jspecify.annotations.Nullable;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.cli.jcommander.samples.issue3566.RunFailureSamples;
import org.testng.internal.Utils;
import test.TestHelper;

@Test(singleThreaded = true)
public class RunFailureCommandLineTest {

  @DataProvider
  public Object[][] failures() throws IOException {
    Path directory = TestHelper.createRandomDirectory();
    Path missing = directory.resolve("missing.xml");
    String malformed = TestHelper.writeSuiteToTempFile("<suite");
    String child =
        TestHelper.writeSuiteToTempFile(
            TestHelper.SUITE_XML_HEADER
                + "<suite name=\"Parent\"><suite-files><suite-file path=\""
                + missing
                + "\"/></suite-files></suite>");
    return new Object[][] {
      {missing.toString(), missing.toString()},
      {malformed, malformed},
      // The parser canonicalizes child suite paths before opening them
      {child, missing.toFile().getCanonicalPath()},
      {TestHelper.writeSuiteToTempFile(suiteContent("no.such.Klass")), "no.such.Klass"},
      {
        TestHelper.writeSuiteToTempFile(
            TestHelper.SUITE_XML_HEADER
                + "<suite name=\"Dup\"><test name=\"T\"/><test name=\"T\"/></suite>"),
        "same name"
      },
      {
        TestHelper.writeSuiteToTempFile(
            TestHelper.SUITE_XML_HEADER
                + "<suite name=\"Listener\"><listeners><listener class-name=\"no.such.Listener\"/>"
                + "</listeners></suite>"),
        "no.such.Listener"
      }
    };
  }

  @Test(dataProvider = "failures", description = "GITHUB-3566")
  public void brokenSuiteReportsFailure(String broken, String diagnostic) throws IOException {
    String valid =
        TestHelper.writeSuiteToTempFile(suiteContent(RunFailureSamples.SimpleTest.class.getName()));
    Output output = capture("-verbose", "0", broken, valid);
    assertSoftly(
        softly -> {
          softly
              .assertThat(output.status)
              .as("CLI exit status, stderr:%n%s", output.stderr)
              .isEqualTo(1);
          softly.assertThat(output.stderr).contains(diagnostic);
        });
  }

  @Test
  public void javaApiRecordsRunFailure() throws IOException {
    Path missing = TestHelper.createRandomDirectory().resolve("missing.xml");
    TestNG testng = new TestNG(false);
    testng.setTestSuites(Collections.singletonList(missing.toString()));
    assertThatThrownBy(testng::run)
        .isInstanceOf(TestNGException.class)
        .hasMessageContaining(missing.toString());
    assertThat(testng.getStatus()).isEqualTo(1);
    assertThat(testng.hasFailure()).isTrue();
  }

  @Test
  public void emptySuiteStillReportsNoTests() throws IOException {
    String suite =
        TestHelper.writeSuiteToTempFile(
            TestHelper.SUITE_XML_HEADER + "<suite name=\"Empty\"><test name=\"Test\"/></suite>");
    assertThat(capture("-verbose", "0", suite).status).isEqualTo(8);
  }

  @Test
  public void failedConfigurationWithoutTestsStillReportsNoTests() throws IOException {
    String suite =
        TestHelper.writeSuiteToTempFile(
            suiteContent(RunFailureSamples.FailingSetupOnly.class.getName()));
    Output output = capture("-verbose", "2", suite);
    assertThat(output.status).as("CLI stderr:%n%s", output.stderr).isEqualTo(8);
    assertThat(output.stderr).contains("No tests found. Nothing was run");
    TestNG testng = new TestNG(false);
    testng.setVerbose(0);
    testng.setTestClasses(new Class<?>[] {RunFailureSamples.FailingSetupOnly.class});
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(8);
  }

  @DataProvider
  public Object[][] verbosity() {
    return new Object[][] {{1, false}, {2, true}};
  }

  @Test(dataProvider = "verbosity")
  public void skippedRunReportsStackTraceOnStderr(int verbose, boolean stackTrace)
      throws IOException {
    Output output =
        capture(
            "-verbose",
            Integer.toString(verbose),
            "-failwheneverythingskipped",
            "-testclass",
            RunFailureSamples.AllSkipped.class.getName());
    assertThat(output.status).isEqualTo(1);
    assertThat(output.stderr).contains("All tests were skipped. Nothing was run.");
    assertThat(output.stderr.contains("\tat org.testng")).isEqualTo(stackTrace);
    assertThat(output.stdout).doesNotContain("\tat org.testng");
  }

  @Test(description = "GITHUB-3566")
  public void mainExitsWithFailure() throws IOException, InterruptedException {
    String malformed = TestHelper.writeSuiteToTempFile("<suite");
    String valid =
        TestHelper.writeSuiteToTempFile(suiteContent(RunFailureSamples.SimpleTest.class.getName()));
    TestHelper.ForkResult result =
        TestHelper.runTestNG(
            Arrays.asList("-Duser.language=de", "-Duser.country=DE"),
            Arrays.asList("-usedefaultlisteners", "false", "-verbose", "0", malformed, valid));
    assertSoftly(
        softly -> {
          softly
              .assertThat(result.exitStatus)
              .as("CLI exit status, stdout:%n%s%nstderr:%n%s", result.stdout, result.stderr)
              .isEqualTo(1);
          softly.assertThat(result.stderr).contains(malformed, "SAXParseException");
        });
  }

  @Test
  public void cyclicFailureCausesAreReportedOnce() throws IOException {
    TestNGException failure =
        new TestNGException("outer") {
          private int causeReads;

          @Override
          public synchronized @Nullable Throwable getCause() {
            if (++causeReads > 4) {
              throw new AssertionError("The cause cycle was traversed repeatedly");
            }
            return super.getCause();
          }
        };
    IllegalStateException inner = new IllegalStateException("inner");
    failure.initCause(inner);
    inner.initCause(failure);
    Output output =
        capture(
            () -> {
              Utils.setVerbose(0);
              new TestNG(false).reportRunFailure(failure);
              return 0;
            });
    assertThat(output.stderr)
        .containsOnlyOnce("Caused by: java.lang.IllegalStateException: inner")
        .doesNotContain("Caused by: org.testng.TestNGException");
  }

  private static String suiteContent(String className) {
    return TestHelper.SUITE_XML_HEADER
        + "<suite name=\"Repro\"><test name=\"Test\"><classes><class name=\""
        + className
        + "\"/></classes></test></suite>";
  }

  private static Output capture(String... args) throws IOException {
    String[] cli = new String[args.length + 2];
    cli[0] = "-usedefaultlisteners";
    cli[1] = "false";
    System.arraycopy(args, 0, cli, 2, args.length);
    return capture(() -> new JCommanderCliRunner().run(cli, null).getStatus());
  }

  private static Output capture(IntSupplier run) throws IOException {
    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;
    int verbose = Utils.getVerbose();
    ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    try (PrintStream out = new PrintStream(stdout, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(stderr, true, StandardCharsets.UTF_8)) {
      System.setOut(out);
      System.setErr(err);
      return new Output(
          run.getAsInt(),
          stdout.toString(StandardCharsets.UTF_8),
          stderr.toString(StandardCharsets.UTF_8));
    } finally {
      System.setOut(originalOut);
      System.setErr(originalErr);
      Utils.setVerbose(verbose);
    }
  }

  private static class Output {
    final int status;
    final String stdout;
    final String stderr;

    Output(int status, String stdout, String stderr) {
      this.status = status;
      this.stdout = stdout;
      this.stderr = stderr;
    }
  }
}
