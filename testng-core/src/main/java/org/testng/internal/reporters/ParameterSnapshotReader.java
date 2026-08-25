package org.testng.internal.reporters;

import java.util.Collection;
import org.testng.IReporter;
import org.testng.ISuite;

/**
 * A built-in reporter that reads the reporting snapshots and has no invocation lifecycle to ask
 * from, so the run asks on its behalf.
 *
 * <p>Which of the two routes a reporter takes follows from where it is registered, not from what it
 * reads:
 *
 * <ul>
 *   <li>registered as an {@link IReporter} on the {@code TestNG} instance -- through {@code
 *       addReporter} or {@code addListener}, which is how every built-in report of {@code
 *       initializeDefaultListeners} arrives -- then it is only ever called once every suite has
 *       finished, and it says so here;
 *   <li>registered on a {@code TestRunner}, as {@code TextReporter} and {@code TestHTMLReporter}
 *       are, then it is told when a context starts and asks for itself with {@link
 *       ParameterSnapshots#requestCaptureFor}. Only it can: whether {@code TextReporter} reads
 *       anything depends on how verbose it was built to be.
 * </ul>
 *
 * <p>Extending {@link IReporter} is what keeps that a rule rather than a convention: the scan below
 * only ever sees the run's reporters, so a class that is not one -- {@code XMLSuiteResultWriter}
 * and the {@code jq} panels are the ones to watch, since they hold the reads but are not reporters
 * -- could otherwise wear this and be quietly passed over, leaving its report to fall back to
 * {@link org.testng.ITestResult#getParameters()} with nothing to say it had. A read that arrives
 * this late is worse off than the fallback suggests: the request below is also what tells the store
 * to hold what the live reporters are finished with, so a reporter missing from this scan reads a
 * store that dropped exactly the results it came for -- every configuration method that passed.
 * That is what {@code Main} will have to declare when the {@code jq} report is migrated; a default
 * run hides it, because {@code XMLReporter} has already asked.
 *
 * <p>Internal, and otherwise empty: implementing it is a statement about TestNG's own reporting,
 * not an extension point. A third party reporter that wants the snapshots implements {@link
 * org.testng.ITestListener} and asks, exactly as {@code TextReporter} does.
 */
public interface ParameterSnapshotReader extends IReporter {

  /**
   * Takes the run's snapshots if any of its reporters reads them.
   *
   * <p>Call it from the one point where every reporter of the run is known and none of its suites
   * has started. Asking there rather than per invocation is what keeps the guarantee worth having:
   * the request precedes the first invocation of the whole run, so a store cannot end up holding
   * some of a suite's invocations and re-reading the rest. It also keeps the cost where it belongs
   * -- a run whose reporters read no snapshot renders nothing, and rendering calls the user's
   * {@code toString()} once per invocation.
   *
   * @param reporters - The reporters the run will report through.
   * @param suites - The suites about to run, none of which has started.
   */
  static void requestCaptureIfAnyReads(
      Collection<? extends IReporter> reporters, Collection<ISuite> suites) {
    if (reporters.stream().anyMatch(ParameterSnapshotReader.class::isInstance)) {
      suites.forEach(ParameterSnapshots::requestCaptureHeldUntilReportingFor);
    }
  }
}
