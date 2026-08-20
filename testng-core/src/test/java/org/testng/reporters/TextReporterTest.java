package org.testng.reporters;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.issue2725.TestClassSample;
import org.testng.reporters.snapshot.ConfigurationParameterSample;
import org.testng.reporters.snapshot.NonCloneableParameterSample;
import org.testng.reporters.snapshot.OverlappingContextsSample;
import org.testng.reporters.snapshot.ParallelParameterSample;
import org.testng.reporters.snapshot.RenderingSample;
import org.testng.reporters.snapshot.SkippedDataDrivenSample;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.reports.GitHub447Sample;

public class TextReporterTest extends SimpleBaseTest {
  @Test(description = "GITHUB-2725")
  public void testCustomAttributes() {
    String content = report(TestClassSample.class);
    String expected =
        "Test Attributes: <code_name, [dragon_warrior-1]>, <code_name, "
            + "[dragon_warrior-2]>, <code_name, [dragon_warrior-3]>, <code_name, [dragon_warrior-4]>,"
            + " <code_name, [dragon_warrior-5]>, <code_name, [dragon_warrior-6]>, <code_name, "
            + "[dragon_warrior-7]>, <code_name, [dragon_warrior-8]>, <code_name, [dragon_warrior-9]>,"
            + " <code_name, [dragon_warrior-10]>";
    assertThat(content).contains(expected);
  }

  @Test(description = "Ordinary parameters render as they always have")
  public void parametersKeepTheirRenderingAndOrder() {
    assertThat(report(RenderingSample.class)).contains("report(\"text\", 42, null)");
  }

  @Test(description = "GITHUB-447")
  public void mutableCloneableParameterKeepsItsInvocationTimeValue() {
    assertThat(report(GitHub447Sample.class))
        .contains("add([], null, \"[null]\")")
        .contains("add([null], dup, \"[null, dup]\")")
        .contains("add([null, dup], dup, \"[null, dup, dup]\")")
        .contains("add([null, dup, dup], str, \"[null, dup, dup, str]\")")
        .contains("add([null, dup, dup, str], null, \"[null, dup, dup, str, null]\")");
  }

  @Test(
      description =
          "GITHUB-447: a mutable parameter no reflective clone can copy is still reported with the"
              + " value its own invocation ran with")
  public void mutableNonCloneableParameterKeepsItsInvocationTimeValue() {
    assertThat(report(NonCloneableParameterSample.class))
        .contains("report(invocation-1)")
        .contains("report(invocation-2)")
        .contains("report(invocation-3)")
        .doesNotContain("report(invocation-4)");
  }

  @Test(description = "A configuration method is reported with what it was announced with")
  public void configurationParametersKeepTheirInvocationTimeValue() {
    assertThat(report(ConfigurationParameterSample.class))
        .contains("prepare([before-configuration])")
        .doesNotContain("prepare([mutated])");
  }

  @Test(description = "Snapshots stay with their own result when the invocations overlap")
  public void parallelInvocationsDoNotShareSnapshots() {
    String content = report(ParallelParameterSample.class);
    for (int row = 0; row < ParallelParameterSample.ROWS; row++) {
      assertThat(content).contains("report(row-" + row + ")");
    }
    assertThat(content).doesNotContain("report(mutated)");
  }

  @Test(
      description =
          "Contexts that overlap each report their own values: one finishing takes nothing from the"
              + " other")
  public void parallelContextsDoNotShareSnapshots() {
    XmlSuite suite = createXmlSuite("overlapping-contexts");
    suite.setParallel(XmlSuite.ParallelMode.TESTS);
    createXmlTest(suite, "first", OverlappingContextsSample.class);
    createXmlTest(suite, "second", OverlappingContextsSample.class);

    assertThat(report(create(suite)))
        .contains("report(first-context)")
        .contains("report(second-context)")
        .doesNotContain("report(mutated)");
  }

  @Test(
      description =
          "A result announced before it was given its values still reports them, through the"
              + " result itself")
  public void resultsWithNothingToSnapshotStillReportTheirParameters() {
    TestNG testng = create(SkippedDataDrivenSample.class);
    testng.setReportAllDataDrivenTestsAsSkipped(true);

    assertThat(report(testng)).contains("skipped(\"first\")").contains("skipped(\"second\")");
  }

  /**
   * Runs the class under a {@link TextReporter} verbose enough to log, and returns what it said.
   */
  private static String report(Class<?> testClass) {
    return report(create(testClass));
  }

  private static String report(TestNG testng) {
    PrintStream currentStream = System.out;
    final Charset charset = StandardCharsets.UTF_8;
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos, true, charset);
      System.setOut(ps);
      testng.addListener(new TextReporter("Example_Test", 2));
      testng.run();
      return baos.toString(charset);
    } finally {
      System.setOut(currentStream);
    }
  }
}
