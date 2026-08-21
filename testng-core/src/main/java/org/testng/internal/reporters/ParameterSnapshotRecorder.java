package org.testng.internal.reporters;

import org.testng.IConfigurationListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Fills a suite's {@link ParameterSnapshots}: the one place a reporting snapshot is taken, so that
 * however many reporters read one, the user's {@code toString()} runs once per invocation.
 *
 * <p>Both lifecycle points it listens to run before the method body does, once the values have been
 * assigned, so what it captures is what the invocation started with -- whatever the method later
 * does to those values:
 *
 * <ul>
 *   <li>{@code onTestStart}: {@code TestInvoker} builds the result with its parameters, sets {@code
 *       STARTED} and only then runs the listeners -- including for the invocations it registers as
 *       skipped without ever running them;
 *   <li>{@code beforeConfiguration}: {@code ConfigInvoker} calls {@code setParameters} immediately
 *       before it runs the configuration listeners. A configuration skipped because an earlier one
 *       failed is the exception: its parameters are never computed, so there is nothing to capture
 *       and nothing to report.
 * </ul>
 *
 * <p>It is installed on every {@code TestRunner} of the suite as a listener, so no invoker has to
 * know that reporting snapshots exist -- ahead of the ones the runner was given, since a reporter
 * being told that an invocation is starting has to find its snapshot already taken.
 */
public final class ParameterSnapshotRecorder implements ITestListener, IConfigurationListener {

  private final ParameterSnapshots snapshots;

  public ParameterSnapshotRecorder(ParameterSnapshots snapshots) {
    this.snapshots = snapshots;
  }

  @Override
  public void onTestStart(ITestResult result) {
    snapshots.captureIfAbsent(result);
  }

  @Override
  public void beforeConfiguration(ITestResult result) {
    snapshots.captureIfAbsent(result);
  }

  @Override
  public void onConfigurationSuccess(ITestResult result) {
    // Only failed and skipped configurations are listed, so nothing will read this one again -- and
    // the reporters that print a configuration as it passes already have: a configuration finishing
    // is dispatched in reverse, which makes this listener, registered first, the last one told.
    snapshots.discard(result);
  }
}
