package org.testng.reporters;

import static org.testng.internal.Utils.isStringNotBlank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.internal.Utils;

/** A simple reporter that collects the results and prints them on standard out. */
public class TextReporter implements ITestListener {

  private static final String LINE = "\n===============================================\n";

  private final int m_verbose;
  private final String m_testName;

  public TextReporter(String testName, int verbose) {
    m_testName = testName;
    m_verbose = verbose;
  }

  @Override
  public void onFinish(ITestContext context) {
    if (m_verbose >= 2) {
      logResults(context);
    }
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
          Utils.detailedMethodName(tr.getMethod(), false),
          tr.getMethod().getDescription(),
          stackTrace,
          tr.getParameters(),
          tr.getMethod().getParameterTypes());
    }

    results = context.getSkippedConfigurations().getAllResults();
    for (ITestResult tr : results) {
      logResult(
          "SKIPPED CONFIGURATION",
          Utils.detailedMethodName(tr.getMethod(), false),
          tr.getMethod().getDescription(),
          null,
          tr.getParameters(),
          tr.getMethod().getParameterTypes());
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
    List<ITestResult> skippedTests = Lists.newArrayList();
    List<ITestResult> retriedTests = Lists.newArrayList();
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

  private void logResult(String status, ITestResult tr, String stackTrace) {
    logResult(
        status,
        tr.getName(),
        tr.getMethod().getDescription(),
        stackTrace,
        tr.getParameters(),
        tr.getMethod().getParameterTypes());
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
      String stackTrace,
      Object[] params,
      Class<?>[] paramTypes) {
    StringBuilder msg = new StringBuilder(name);

    if (null != params && params.length > 0) {
      msg.append("(");

      // The error might be a data provider parameter mismatch, so make
      // a special case here
      if (params.length != paramTypes.length) {
        msg.append(name)
            .append(": Wrong number of arguments were passed by ")
            .append("the Data Provider: found ")
            .append(params.length)
            .append(" but ")
            .append("expected ")
            .append(paramTypes.length)
            .append(")");
      } else {
        for (int i = 0; i < params.length; i++) {
          if (i > 0) {
            msg.append(", ");
          }
          msg.append(Utils.toString(params[i], paramTypes[i]));
        }

        msg.append(")");
      }
    }
    if (!Utils.isStringEmpty(description)) {
      msg.append("\n");
      for (int i = 0; i < status.length() + 2; i++) {
        msg.append(" ");
      }
      msg.append(description);
    }
    if (!Utils.isStringEmpty(stackTrace)) {
      msg.append("\n").append(stackTrace);
    }

    logResult(status, msg.toString());
  }
}
