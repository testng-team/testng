package org.testng.reporters;

import static org.testng.internal.Utils.isStringNotBlank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.CustomAttribute;
import org.testng.internal.Utils;

/** A simple reporter that collects the results and prints them on standard out. */
public class TextReporter implements ITestListener, IConfigurationListener {

  private static final String LINE = "\n===============================================\n";

  private final int m_verbose;
  private final String m_testName;
  private final ParameterSnapshots m_parameterSnapshots = new ParameterSnapshots();

  public TextReporter(String testName, int verbose) {
    m_testName = testName;
    m_verbose = verbose;
  }

  @Override
  public void onTestStart(ITestResult tr) {
    captureParameters(tr);
  }

  @Override
  public void beforeConfiguration(ITestResult tr) {
    captureParameters(tr);
  }

  @Override
  public void onConfigurationSuccess(ITestResult tr) {
    // Only failed and skipped configurations are listed, so this one is never going to be printed.
    m_parameterSnapshots.discard(tr);
  }

  /**
   * Both lifecycle points run before the method body does, so what is captured here is what the
   * invocation started with, whatever it later does to those values.
   */
  private void captureParameters(ITestResult tr) {
    if (logsResults()) {
      // Otherwise this reporter prints nothing, and rendering a value would run the user's
      // toString() for output nobody asked for.
      m_parameterSnapshots.capture(tr);
    }
  }

  @Override
  public void onFinish(ITestContext context) {
    try {
      if (logsResults()) {
        logResults(context);
      }
    } finally {
      m_parameterSnapshots.discard(context);
    }
  }

  /** Whether this reporter is verbose enough to print anything, and so to capture anything. */
  private boolean logsResults() {
    return m_verbose >= 2;
  }

  private static List<ITestNGMethod> resultsToMethods(Collection<ITestResult> results) {
    return results.stream().map(ITestResult::getMethod).collect(Collectors.toList());
  }

  private void logResults(ITestContext context) {
    // Log Text
    Set<ITestResult> results = context.getFailedConfigurations().getAllResults();
    for (ITestResult tr : results) {
      Throwable ex = tr.getThrowable();
      String stackTrace = "";
      if (ex != null && m_verbose >= 2) {
        stackTrace = Utils.shortStackTrace(ex, false);
      }

      logResult(
          "FAILED CONFIGURATION",
          Utils.detailedMethodName(tr.getMethod()),
          tr.getMethod().getDescription(),
          tr.getMethod().getAttributes(),
          stackTrace,
          reportedParametersOf(tr));
    }

    results = context.getSkippedConfigurations().getAllResults();
    for (ITestResult tr : results) {
      logResult(
          "SKIPPED CONFIGURATION",
          Utils.detailedMethodName(tr.getMethod()),
          tr.getMethod().getDescription(),
          tr.getMethod().getAttributes(),
          null,
          reportedParametersOf(tr));
    }

    results = context.getPassedTests().getAllResults();
    for (ITestResult tr : results) {
      logResult("PASSED", tr, null);
    }

    results = context.getFailedTests().getAllResults();
    for (ITestResult tr : results) {
      Throwable ex = tr.getThrowable();
      String stackTrace = "";
      if (ex != null && m_verbose >= 2) {
        stackTrace = Utils.shortStackTrace(ex, false);
      }

      logResult("FAILED", tr, stackTrace);
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

    logExceptions("SKIPPED", skippedTests);
    logExceptions("RETRIED", retriedTests);

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

  private void logResult(String status, ITestResult tr, @Nullable String stackTrace) {
    logResult(
        status,
        tr.getMethod().getQualifiedName(),
        tr.getMethod().getDescription(),
        tr.getMethod().getAttributes(),
        stackTrace,
        reportedParametersOf(tr));
  }

  /**
   * The values an invocation ran with, as this reporter saw them when it started.
   *
   * <p>Falls back to the result's own representation for the invocations announced before they were
   * given their values, which leaves nothing to capture: the results {@code
   * reportAllDataDrivenTestsAsSkipped} parameterizes after announcing them, and a configuration
   * method skipped before its parameters were computed. Those keep reading through {@link
   * org.testng.ITestResult#getParameters()}, exactly as every result did before.
   */
  private @Nullable ParameterSnapshot reportedParametersOf(ITestResult tr) {
    ParameterSnapshot captured = m_parameterSnapshots.find(tr);
    return captured != null
        ? captured
        : ParameterSnapshot.of(tr.getParameters(), tr.getMethod().getParameterTypes());
  }

  private void logExceptions(String status, List<ITestResult> results) {
    results.forEach(
        tr -> {
          Throwable throwable = tr.getThrowable();
          logResult(status, tr, throwable != null ? Utils.shortStackTrace(throwable, false) : null);
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
      String description,
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
