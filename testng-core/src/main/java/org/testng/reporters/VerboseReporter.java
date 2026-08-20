package org.testng.reporters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.CustomAttribute;
import org.testng.internal.Utils;
import org.testng.internal.reporters.ParameterSnapshot;
import org.testng.internal.reporters.ParameterSnapshots;

/**
 * Reporter printing out detailed messages about what TestNG is going to run and what is the status
 * of what has been just run.
 *
 * <p>To see messages from this reporter, either run Ant in verbose mode ('ant -v') or set verbose
 * level to 5 or higher
 *
 * @since 6.4
 */
public class VerboseReporter implements IConfigurationListener, ITestListener {

  /** Default prefix for messages printed out by this reporter */
  public static final String LISTENER_PREFIX = "[VerboseTestNG] ";

  /** Set by {@link #onStart} and cleared by {@link #onFinish}: null between suites. */
  private @Nullable String suiteName;

  private final String prefix;

  private enum Status {
    SUCCESS,
    FAILURE,
    SKIP,
    SUCCESS_PERCENTAGE_FAILURE,
    STARTED
  }

  /**
   * Create VerboseReporter with custom prefix
   *
   * @param prefix prefix for messages printed out by this reporter
   */
  public VerboseReporter(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public void beforeConfiguration(ITestResult tr) {
    logTestResult(Status.STARTED, tr, true);
  }

  @Override
  public void onConfigurationFailure(ITestResult tr) {
    logTestResult(Status.FAILURE, tr, true);
  }

  @Override
  public void onConfigurationSkip(ITestResult tr) {
    logTestResult(Status.SKIP, tr, true);
  }

  @Override
  public void onConfigurationSuccess(ITestResult tr) {
    logTestResult(Status.SUCCESS, tr, true);
  }

  @Override
  public void onTestStart(ITestResult tr) {
    logTestResult(Status.STARTED, tr, false);
  }

  @Override
  public void onTestFailure(ITestResult tr) {
    logTestResult(Status.FAILURE, tr, false);
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult tr) {
    logTestResult(Status.SUCCESS_PERCENTAGE_FAILURE, tr, false);
  }

  @Override
  public void onTestSkipped(ITestResult tr) {
    logTestResult(Status.SKIP, tr, false);
  }

  @Override
  public void onTestSuccess(ITestResult tr) {
    logTestResult(Status.SUCCESS, tr, false);
  }

  /**
   * This reporter prints every invocation it is told about, so it always reads the suite's
   * snapshots and asks for them unconditionally -- where {@link TextReporter} only does so above
   * verbose 2.
   */
  @Override
  public void onStart(ITestContext ctx) {
    suiteName = ctx.getName();
    ParameterSnapshots.requestCaptureFor(ctx.getSuite());
    log(
        "RUNNING: Suite: \""
            + suiteName
            + "\" containing \""
            + ctx.getAllTestMethods().length
            + "\" Tests (config: "
            + ctx.getSuite().getXmlSuite().getFileName()
            + ")");
  }

  @Override
  public void onFinish(ITestContext context) {
    logResults(context);
    suiteName = null;
  }

  private ITestNGMethod[] resultsToMethods(Collection<ITestResult> results) {
    return results.stream().map(ITestResult::getMethod).toArray(ITestNGMethod[]::new);
  }

  /** Print out test summary */
  private void logResults(ITestContext context) {
    //
    // Log test summary
    //
    ITestNGMethod[] ft = resultsToMethods(context.getFailedTests().getAllResults());
    StringBuilder sb = new StringBuilder("\n===============================================\n");
    sb.append("    ").append(suiteName).append("\n");
    sb.append("    Tests run: ").append(context.getAllTestMethods().length);
    sb.append(", Failures: ").append(ft.length);
    sb.append(", Skips: ")
        .append(Arrays.toString(resultsToMethods(context.getSkippedTests().getAllResults())));
    int confFailures = context.getFailedConfigurations().size();
    int confSkips = context.getSkippedConfigurations().size();
    if (confFailures > 0 || confSkips > 0) {
      sb.append("\n").append("    Configuration Failures: ").append(confFailures);
      sb.append(", Skips: ").append(confSkips);
    }
    sb.append("\n===============================================");
    log(sb.toString());
  }

  /**
   * Log meaningful message for passed in arguments. Message itself is of form: $status:
   * "$suiteName" - $methodDeclaration ($actualArguments) finished in $x ms ($run of $totalRuns)
   *
   * @param st status of passed in itr
   * @param itr test result to be described
   * @param isConfMethod is itr describing configuration method
   */
  private void logTestResult(Status st, ITestResult itr, boolean isConfMethod) {
    StringBuilder sb = new StringBuilder();
    String stackTrace = "";
    switch (st) {
      case STARTED:
        sb.append("INVOKING");
        break;
      case SKIP:
        sb.append("SKIPPED");
        stackTrace =
            itr.getThrowable() != null ? Utils.shortStackTrace(itr.getThrowable(), false) : "";
        break;
      case FAILURE:
        sb.append("FAILED");
        stackTrace =
            itr.getThrowable() != null ? Utils.shortStackTrace(itr.getThrowable(), false) : "";
        break;
      case SUCCESS:
        sb.append("PASSED");
        break;
      case SUCCESS_PERCENTAGE_FAILURE:
        sb.append("PASSED with failures");
        break;
      default:
        // not happen
        throw new RuntimeException("Unsupported test status:" + itr.getStatus());
    }
    if (isConfMethod) {
      sb.append(" CONFIGURATION: ");
    } else {
      sb.append(": ");
    }
    ITestNGMethod tm = itr.getMethod();
    int identLevel = sb.length();
    sb.append(getMethodDeclaration(tm));
    ParameterSnapshot params = reportedParametersOf(itr);
    if (null != params) {
      // The error might be a data provider parameter mismatch, so make
      // a special case here
      if (params.hasCountMismatch()) {
        sb.append("Wrong number of arguments were passed by the Data Provider: found ");
        sb.append(params.suppliedCount());
        sb.append(" but expected ");
        sb.append(params.expectedCount());
      } else {
        sb.append("(value(s): ");
        sb.append(String.join(", ", params.renderedValues()));
        sb.append(")");
      }
    }
    if (Status.STARTED != st) {
      sb.append(" finished in ");
      sb.append(itr.getEndMillis() - itr.getStartMillis());
      sb.append(" ms");
      if (!Utils.isStringEmpty(tm.getDescription())) {
        sb.append("\n");
        sb.append(" ".repeat(Math.max(0, identLevel)));
        sb.append(tm.getDescription());
      }
      if (tm.getInvocationCount() > 1) {
        sb.append(" (");
        sb.append(tm.getCurrentInvocationCount());
        sb.append(" of ");
        sb.append(tm.getInvocationCount());
        sb.append(")");
      }
      if (!Utils.isStringEmpty(stackTrace)) {
        sb.append("\n")
            .append(
                stackTrace, 0, stackTrace.lastIndexOf(RuntimeBehavior.getDefaultLineSeparator()));
      }
    } else {
      if (!isConfMethod && tm.getInvocationCount() > 1) {
        sb.append(" success: ");
        sb.append(tm.getSuccessPercentage());
        sb.append("%");
      }
    }
    CustomAttribute[] attributes = tm.getAttributes();
    if ((st != Status.STARTED) && (attributes != null && attributes.length != 0)) {
      String text =
          "Test Attributes: "
              + Arrays.stream(attributes)
                  .map(
                      attribute ->
                          "<" + attribute.name() + ", " + Arrays.toString(attribute.values()) + ">")
                  .collect(Collectors.joining(","));
      sb.append("\n").append(text);
    }
    log(sb.toString());
  }

  /**
   * Looked up per result: printing as a run happens, there is no one moment to resolve a store.
   *
   * <p>A result carries no context when it was built outside one -- the parameter carrier a
   * configuration method is handed, which exists before the invocation it reports on is bound to a
   * context. There is no suite to ask, so such a result reads through itself, like any other the
   * snapshots have nothing for.
   */
  private static @Nullable ParameterSnapshot reportedParametersOf(ITestResult itr) {
    ITestContext context = itr.getTestContext();
    return ParameterSnapshots.reportedParametersOf(
        context != null ? ParameterSnapshots.of(context.getSuite()) : null, itr);
  }

  protected void log(String message) {
    // prefix all output lines
    System.out.println(message.replaceAll("(?m)^", prefix));
  }

  /**
   * @param method method to be described
   * @return FQN of a class + method declaration for a method passed in ie.
   *     test.triangle.CheckCount.testCheckCount(java.lang.String)
   */
  private String getMethodDeclaration(ITestNGMethod method) {

    // see Utils.detailedMethodName
    // perhaps should rather adopt the original method instead
    StringBuilder buf = new StringBuilder();
    buf.append("\"");
    buf.append(Optional.ofNullable(suiteName).orElse("UNKNOWN"));
    buf.append("\"");
    buf.append(" - ");
    String tempName = Utils.annotationFormFor(method);
    if (!tempName.isEmpty()) {
      buf.append(Utils.annotationFormFor(method)).append(" ");
    }
    buf.append(method.getQualifiedName());
    Class<?>[] objects = method.getParameterTypes();
    buf.append("(").append(Utils.stringifyTypes(objects)).append(")");
    return buf.toString();
  }

  @Override
  public String toString() {
    return "VerboseReporter{" + "suiteName=" + suiteName + '}';
  }
}
