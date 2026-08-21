package org.testng.reporters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.snapshot.ConfigurationParameterSample;
import org.testng.reporters.snapshot.NonCloneableParameterSample;
import org.testng.reporters.snapshot.ParallelParameterSample;
import org.testng.reporters.snapshot.PassingConfigurationParameterSample;
import org.testng.reporters.snapshot.RenderingCountSample;
import org.testng.reporters.snapshot.RenderingSample;
import org.testng.reporters.snapshot.SkippedDataDrivenSample;
import org.testng.reporters.snapshot.WrongArgumentCountSample;
import test.SimpleBaseTest;
import test.reports.GitHub447Sample;

/**
 * What this reporter says an invocation ran with. Unlike {@link TextReporter} it prints as the run
 * happens, twice per invocation: once as it starts and once as it finishes. The assertions below
 * are on the second line -- the one ending in {@code finished in} -- since that is the one the
 * shared snapshot exists for: by then the test has had every chance to change what it was given.
 */
public class VerboseReporterTest extends SimpleBaseTest {

  @Test(description = "Ordinary parameters render as they always have")
  public void parametersKeepTheirRenderingAndOrder() {
    assertThat(report(RenderingSample.class))
        .contains("(value(s): \"text\", 42, null) finished in");
  }

  @Test(
      description =
          "GITHUB-447: a mutable parameter no reflective clone can copy is still reported with the"
              + " value its own invocation ran with")
  public void mutableNonCloneableParameterKeepsItsInvocationTimeValue() {
    assertThat(report(NonCloneableParameterSample.class))
        .contains("(value(s): invocation-1) finished in")
        .contains("(value(s): invocation-2) finished in")
        .contains("(value(s): invocation-3) finished in")
        .doesNotContain("invocation-4");
  }

  @Test(description = "GITHUB-447")
  public void mutableCloneableParameterKeepsItsInvocationTimeValue() {
    assertThat(report(GitHub447Sample.class))
        .contains("(value(s): [], null, \"[null]\") finished in")
        .contains("(value(s): [null], dup, \"[null, dup]\") finished in")
        .contains("(value(s): [null, dup], dup, \"[null, dup, dup]\") finished in")
        .contains("(value(s): [null, dup, dup], str, \"[null, dup, dup, str]\") finished in")
        .contains(
            "(value(s): [null, dup, dup, str], null, \"[null, dup, dup, str, null]\") finished in");
  }

  @Test(
      description =
          "A configuration method that failed is reported with what it was announced" + " with")
  public void failedConfigurationParametersKeepTheirInvocationTimeValue() {
    assertThat(report(ConfigurationParameterSample.class))
        .contains("FAILED CONFIGURATION")
        .contains("prepare([Ljava.lang.Object;)(value(s): [before-configuration]) finished in")
        .doesNotContain("prepare([Ljava.lang.Object;)(value(s): [mutated])");
  }

  @Test(
      description =
          "A configuration method that passed is reported with what it was announced with too: its"
              + " snapshot is dropped only once every reporter has been told")
  public void passedConfigurationParametersKeepTheirInvocationTimeValue() {
    assertThat(report(PassingConfigurationParameterSample.class))
        .contains("PASSED CONFIGURATION")
        .contains("prepare([Ljava.lang.Object;)(value(s): [before-configuration]) finished in")
        .doesNotContain("prepare([Ljava.lang.Object;)(value(s): [mutated])");
  }

  @Test(description = "Snapshots stay with their own result when the invocations overlap")
  public void parallelInvocationsDoNotShareSnapshots() {
    String content = report(ParallelParameterSample.class);
    for (int row = 0; row < ParallelParameterSample.ROWS; row++) {
      assertThat(content).contains("(value(s): row-" + row + ") finished in");
    }
    assertThat(content).doesNotContain("(value(s): mutated)");
  }

  @Test(
      description =
          "A data-driven test skipped by a dependency reports the row it would have run, from the"
              + " snapshot taken when it was announced")
  public void dataDrivenSkipsReportTheValuesTheyWereAnnouncedWith() {
    TestNG testng = create(SkippedDataDrivenSample.class);
    testng.setReportAllDataDrivenTestsAsSkipped(true);

    assertThat(report(testng))
        .contains("(value(s): \"first\") finished in")
        .contains("(value(s): \"second\") finished in");
  }

  @Test(
      description =
          "A data provider handing over the wrong number of values fails the method before it is"
              + " given any, so there is nothing to report for it")
  public void aResultThatNeverGotItsValuesReportsNone() {
    assertThat(report(WrongArgumentCountSample.class))
        .contains("Data provider mismatch")
        .contains("WrongArgumentCountSample.report(java.lang.String) finished in")
        .doesNotContain("value(s)");
  }

  @Test(
      description =
          "Both built-in reporters print what an invocation ran with, and between them the value is"
              + " rendered once")
  public void bothReportersReadTheOneSnapshot() {
    TestNG testng = create(RenderingCountSample.class);
    testng.addListener(new TextReporter("Example_Test", 2));
    int renderedBefore = RenderingCountSample.renderings();

    String content = report(testng);

    // Three printed mentions of the value: this reporter's two lines, and TextReporter's one.
    assertThat(content).contains("(value(s): counted) finished in").contains("report(counted)");
    assertThat(RenderingCountSample.renderings() - renderedBefore).isEqualTo(1);
  }

  /** Runs the class under a {@link VerboseReporter}, and returns what it said. */
  private static String report(Class<?> testClass) {
    return report(create(testClass));
  }

  private static String report(TestNG testng) {
    return ReportedOutput.of(testng, new VerboseReporter(VerboseReporter.LISTENER_PREFIX));
  }
}
