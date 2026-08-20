package org.testng.reporters;

import static org.testng.internal.Utils.isStringNotBlank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.CustomAttribute;
import org.testng.internal.Utils;
import org.testng.internal.reporters.ParameterSnapshot;
import org.testng.internal.reporters.ParameterSnapshots;

/** A simple reporter that collects the results and prints them on standard out. */
public class TextReporter implements ITestListener {

  private static final String LINE = "\n===============================================\n";

  private final int m_verbose;
  private final @Nullable String m_testName;

  public TextReporter(@Nullable String testName, int verbose) {
    m_testName = testName;
    m_verbose = verbose;
  }

  /**
   * Below verbose 2 this reporter prints nothing, so it asks for the suite's snapshots only when it
   * will read them.
   *
   * <p>Early enough: a context starts before its own {@code @BeforeTest} configurations and before
   * any of its invocations. The only thing announced earlier is a suite level configuration method,
   * and those are handed no injected value at all -- only {@code @Parameters} strings, which read
   * the same whenever they are rendered.
   */
  @Override
  public void onStart(ITestContext context) {
    if (logsResults()) {
      ParameterSnapshots snapshots = ParameterSnapshots.of(context.getSuite());
      if (snapshots != null) {
        snapshots.requestCapture();
      }
    }
  }

  @Override
  public void onFinish(ITestContext context) {
    if (logsResults()) {
      logResults(context);
    }
  }

  /** Whether this reporter is verbose enough to print anything. */
  private boolean logsResults() {
    return m_verbose >= 2;
  }

  private static List<ITestNGMethod> resultsToMethods(Collection<ITestResult> results) {
    return results.stream().map(ITestResult::getMethod).collect(Collectors.toList());
  }

  private void logResults(ITestContext context) {
    // The suite's snapshots, resolved once: every result of this context reads the same store.
    ParameterSnapshots snapshots = ParameterSnapshots.of(context.getSuite());

    // Log Text
    Set<ITestResult> results = context.getFailedConfigurations().getAllResults();
    for (ITestResult tr : results) {
      ITestNGMethod method = tr.getMethod();
      Throwable ex = tr.getThrowable();
      String stackTrace = "";
      if (ex != null && m_verbose >= 2) {
        stackTrace = Utils.shortStackTrace(ex, false);
      }

      logResult(
          "FAILED CONFIGURATION",
          Utils.detailedMethodName(method),
          method.getDescription(),
          method.getAttributes(),
          stackTrace,
          reportedParametersOf(snapshots, tr));
    }

    results = context.getSkippedConfigurations().getAllResults();
    for (ITestResult tr : results) {
      ITestNGMethod method = tr.getMethod();
      logResult(
          "SKIPPED CONFIGURATION",
          Utils.detailedMethodName(method),
          method.getDescription(),
          method.getAttributes(),
          null,
          reportedParametersOf(snapshots, tr));
    }

    results = context.getPassedTests().getAllResults();
    for (ITestResult tr : results) {
      logResult(snapshots, "PASSED", tr, null);
    }

    results = context.getFailedTests().getAllResults();
    for (ITestResult tr : results) {
      Throwable ex = tr.getThrowable();
      String stackTrace = "";
      if (ex != null && m_verbose >= 2) {
        stackTrace = Utils.shortStackTrace(ex, false);
      }

      logResult(snapshots, "FAILED", tr, stackTrace);
    }

    results = context.getSkippedTests().getAllResults();
    List<ITestResult> rawskipped = new ArrayList<>(results);
    List<ITestResult> skippedTests = new ArrayList<>();
    List<ITestResult> retriedTests = new ArrayList<>();
    for (ITestResult result : rawskipped) {
      if (result.wasRetried()) {
        retriedTests.add(result);
      } else {
        skippedTests.add(result);
      }
    }

    logExceptions(snapshots, "SKIPPED", skippedTests);
    logExceptions(snapshots, "RETRIED", retriedTests);

    List<ITestNGMethod> ft = resultsToMethods(context.getFailedTests().getAllResults());
    StringBuilder logBuf = new StringBuilder(LINE);
    logBuf.append("    ").append(m_testName).append("\n");
    logBuf
        .append("    Tests run: ")
        .append(context.getAllTestMethods().length)
        .append(", Failures: ")
        .append(ft.size())
        .append(", Skips: ")
        .append(resultsToMethods(skippedTests).size());
    if (!retriedTests.isEmpty()) {
      logBuf.append(", Retries: ").append(resultsToMethods(retriedTests).size());
    }
    int confFailures = context.getFailedConfigurations().size();
    int confSkips = context.getSkippedConfigurations().size();
    if (confFailures > 0 || confSkips > 0) {
      logBuf
          .append("\n")
          .append("    Configuration Failures: ")
          .append(confFailures)
          .append(", Skips: ")
          .append(confSkips);
    }
    logBuf.append(LINE);
    logResult("", logBuf.toString());
  }

  private void logResult(
      @Nullable ParameterSnapshots snapshots,
      String status,
      ITestResult tr,
      @Nullable String stackTrace) {
    ITestNGMethod method = tr.getMethod();
    logResult(
        status,
        method.getQualifiedName(),
        method.getDescription(),
        method.getAttributes(),
        stackTrace,
        reportedParametersOf(snapshots, tr));
  }

  /**
   * The values an invocation ran with, as TestNG captured them when it started.
   *
   * <p>Falls back to the result's own representation for the invocations announced before they were
   * given their values, which leaves nothing to capture: the results {@code
   * reportAllDataDrivenTestsAsSkipped} parameterizes after announcing them, and a configuration
   * method skipped before its parameters were computed. Those keep reading through {@link
   * org.testng.ITestResult#getParameters()}, exactly as every result did before -- as does a result
   * from a suite that has no snapshots at all.
   */
  private @Nullable ParameterSnapshot reportedParametersOf(
      @Nullable ParameterSnapshots snapshots, ITestResult tr) {
    ParameterSnapshot captured = snapshots != null ? snapshots.find(tr) : null;
    return captured != null
        ? captured
        : ParameterSnapshot.of(tr.getParameters(), tr.getMethod().getParameterTypes());
  }

  private void logExceptions(
      @Nullable ParameterSnapshots snapshots, String status, List<ITestResult> results) {
    results.forEach(
        tr -> {
          Throwable throwable = tr.getThrowable();
          logResult(
              snapshots,
              status,
              tr,
              throwable != null ? Utils.shortStackTrace(throwable, false) : null);
        });
  }

  private void logResult(String status, String message) {
    StringBuilder buf = new StringBuilder();
    if (isStringNotBlank(status)) {
      buf.append(status).append(": ");
    }
    buf.append(message);

    System.out.println(buf);
  }

  private void logResult(
      String status,
      String name,
      @Nullable String description,
      CustomAttribute[] attributes,
      @Nullable String stackTrace,
      @Nullable ParameterSnapshot params) {
    StringBuilder msg = new StringBuilder(name);

    if (null != params) {
      msg.append("(");

      // The error might be a data provider parameter mismatch, so make
      // a special case here
      if (params.hasCountMismatch()) {
        msg.append(name)
            .append(": Wrong number of arguments were passed by ")
            .append("the Data Provider: found ")
            .append(params.suppliedCount())
            .append(" but ")
            .append("expected ")
            .append(params.expectedCount())
            .append(")");
      } else {
        msg.append(String.join(", ", params.renderedValues())).append(")");
      }
    }
    if (!Utils.isStringEmpty(description)) {
      msg.append("\n");
      for (int i = 0; i < status.length() + 2; i++) {
        msg.append(" ");
      }
      msg.append(description);
    }
    if (attributes != null && attributes.length != 0) {
      msg.append("\nTest Attributes: ");
      String testAttributes =
          Arrays.stream(attributes)
              .map(
                  attribute ->
                      "<" + attribute.name() + ", " + Arrays.toString(attribute.values()) + ">")
              .collect(Collectors.joining(", "));
      msg.append(testAttributes).append("\n");
    }
    if (!Utils.isStringEmpty(stackTrace)) {
      msg.append("\n").append(stackTrace);
    }

    logResult(status, msg.toString());
  }
}
