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
 *       STARTED} and only then runs the listeners;
 *   <li>{@code beforeConfiguration}: {@code ConfigInvoker} calls {@code setParameters} immediately
 *       before it runs the configuration listeners.
 * </ul>
 *
 * <p>It is installed on every {@code TestRunner} of the suite as an ordinary listener, so no
 * invoker has to know that reporting snapshots exist.
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
    // Only failed and skipped configurations are listed, so this one is never going to be printed.
    snapshots.discard(result);
  }
}
