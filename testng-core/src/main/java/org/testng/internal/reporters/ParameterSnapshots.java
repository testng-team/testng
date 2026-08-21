package org.testng.internal.reporters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;

/**
 * The {@link ParameterSnapshot} taken for each invocation of a suite, produced once and read by
 * every built-in reporter that needs it.
 *
 * <p>One store per {@link ISuite}. That is the narrowest object that outlives what reporting needs
 * it for: it spans the invocations of all of the suite's {@code <test>}s, the listeners those
 * runners carry, and the {@link org.testng.IReporter} pass, which runs only once every context has
 * finished. Finishing an {@link ITestContext} therefore drops nothing; the store is released by
 * {@link #detachFrom}, once the whole run has reported. Two runs cannot share one, since a suite
 * belongs to the {@code TestNG} instance that built it.
 *
 * <p>Nothing is written to {@link ITestResult}, and a result whose snapshot is missing is not an
 * error: a reporter falls back to {@link ITestResult#getParameters()}, which is what it read
 * before.
 *
 * <p>Results are held by identity. {@link ITestResult} is a public interface, so an implementation
 * is free to define {@code equals()} in a way that makes two invocations of the same method with
 * the same values indistinguishable, and reporting must not let that merge their snapshots.
 *
 * <p>Capture is opt-in: rendering a value runs the user's {@code toString()}, which is pure cost
 * when no reporter is verbose enough to print it. A consumer says so once with {@link
 * #requestCapture()}.
 */
public final class ParameterSnapshots {

  /**
   * Mirrors {@code IObjectDispenser.GUICE_HELPER}: an internal object parked on a TestNG attribute
   * under a name of TestNG's own.
   */
  private static final String ATTRIBUTE = "testng.reporting-parameter-snapshots";

  private final Map<ResultKey, ParameterSnapshot> snapshots = new ConcurrentHashMap<>();
  private volatile boolean captureRequested;

  /**
   * Gives a suite the store its invocations will fill and its reporters will read.
   *
   * @param suite - The suite that is about to run.
   * @return - Its store, so the caller can hand it to the recorder without looking it up again.
   */
  public static ParameterSnapshots attachTo(ISuite suite) {
    ParameterSnapshots parameterSnapshots = new ParameterSnapshots();
    suite.setAttribute(ATTRIBUTE, parameterSnapshots);
    return parameterSnapshots;
  }

  /**
   * Releases a suite's store: unhooked, and emptied, so that a reporter which kept a reference to
   * it during {@code generateReport} does not keep every result of the run with it.
   *
   * @param suite - The suite that has finished reporting.
   */
  public static void detachFrom(ISuite suite) {
    ParameterSnapshots parameterSnapshots = of(suite);
    if (parameterSnapshots != null) {
      parameterSnapshots.snapshots.clear();
    }
    suite.removeAttribute(ATTRIBUTE);
  }

  /**
   * @param suite - The suite being reported, or {@code null}. The attribute is read defensively:
   *     the name lives in the suite's public attribute space, so a user can overwrite it.
   * @return - Its store, or {@code null} if it has none -- a suite this class never attached to.
   */
  public static @Nullable ParameterSnapshots of(@Nullable ISuite suite) {
    if (suite == null) {
      return null;
    }
    Object attribute = suite.getAttribute(ATTRIBUTE);
    return attribute instanceof ParameterSnapshots ? (ParameterSnapshots) attribute : null;
  }

  /** Declares that a reporter will read these snapshots, so they are worth taking. */
  public void requestCapture() {
    captureRequested = true;
  }

  /**
   * The same, for a reporter that has a suite rather than a store: a suite without one is a suite
   * whose snapshots nobody is going to take, which is not the reporter's business.
   *
   * <p>Call it from {@link org.testng.ITestListener#onStart(org.testng.ITestContext)}, which is
   * early enough: a context starts before its own {@code @BeforeTest} configurations and before any
   * of its invocations. The only thing announced earlier is a suite level configuration method, and
   * those are handed no injected value at all -- only {@code @Parameters} strings, which read the
   * same whenever they are rendered.
   *
   * @param suite - The suite about to run the invocations the caller will report.
   */
  public static void requestCaptureFor(@Nullable ISuite suite) {
    ParameterSnapshots snapshots = of(suite);
    if (snapshots != null) {
      snapshots.requestCapture();
    }
  }

  /**
   * Captures what {@code result} was invoked with, unless it already has been. Must be called from
   * a lifecycle point the invocation has not run past yet.
   *
   * <p>Idempotent, so that two internal paths announcing the same invocation cannot render it
   * twice. The check is deliberately not a {@code computeIfAbsent}: rendering calls the user's
   * {@code toString()}, and {@link ConcurrentHashMap} requires the mapping function to be short and
   * to touch nothing else. A given result is captured on its own invocation's thread, so there is
   * no concurrent capture of the same key to lose a race with.
   *
   * @param result - The result an invocation is starting with.
   */
  public void captureIfAbsent(ITestResult result) {
    if (!captureRequested) {
      return;
    }
    ResultKey key = new ResultKey(result);
    if (snapshots.containsKey(key)) {
      return;
    }
    ParameterSnapshot snapshot;
    try {
      snapshot =
          ParameterSnapshot.of(result.getParameters(), result.getMethod().getParameterTypes());
    } catch (Throwable rendering) {
      // Rendering a value calls the user's toString(). One that throws would otherwise fail the
      // invocation it is only being reported on; leaving the result unsnapshotted hands it back to
      // the reporter's fallback.
      return;
    }
    if (snapshot != null) {
      snapshots.put(key, snapshot);
    }
  }

  /**
   * @param result - The result being reported.
   * @return - What was captured for it, or {@code null} if nothing was.
   */
  public @Nullable ParameterSnapshot find(ITestResult result) {
    return snapshots.get(new ResultKey(result));
  }

  /**
   * What a reporter prints for a result: the values its invocation ran with, as TestNG captured
   * them when it started.
   *
   * <p>Falls back to the result's own representation when there is no snapshot to read. An
   * invocation is announced with the values it will be reported with, so what remains is a result
   * nothing was captured for:
   *
   * <ul>
   *   <li>a configuration method skipped because an earlier one failed: its parameters are never
   *       computed, since resolving them for a method that will not run can itself fail;
   *   <li>an invocation nothing was ever resolved for -- a lazy factory instance whose construction
   *       failed, or a non-data-driven method skipped by a dependency;
   *   <li>a result from a suite with no store at all, or one whose capture was never requested or
   *       threw while rendering.
   * </ul>
   *
   * <p>Those keep reading through {@link ITestResult#getParameters()}, exactly as every result did
   * before.
   *
   * @param snapshots - The store of the suite being reported, or {@code null} if it has none.
   * @param result - The result being reported.
   * @return - Its rendering, or {@code null} when there is nothing to report.
   */
  public static @Nullable ParameterSnapshot reportedParametersOf(
      @Nullable ParameterSnapshots snapshots, ITestResult result) {
    ParameterSnapshot captured = snapshots != null ? snapshots.find(result) : null;
    return captured != null
        ? captured
        : ParameterSnapshot.of(result.getParameters(), result.getMethod().getParameterTypes());
  }

  /**
   * Drops what was captured for a result nothing will be reported about anymore -- a configuration
   * method that succeeded, which no reporter lists once the run is over. It is the caller's
   * business to know that the live reporters are done with it; see {@link
   * ParameterSnapshotRecorder#onConfigurationSuccess}. Guarded like {@link #captureIfAbsent}:
   * {@code @BeforeMethod} / {@code @AfterMethod} invocations are the most frequent events in a run,
   * and there is nothing to drop when nothing is being captured.
   *
   * @param result - The result nothing will be reported about anymore.
   */
  public void discard(ITestResult result) {
    if (!captureRequested) {
      return;
    }
    snapshots.remove(new ResultKey(result));
  }

  /** @return - Whether anything is still held, which is what {@link #detachFrom} leaves behind. */
  public boolean isEmpty() {
    return snapshots.isEmpty();
  }

  private static final class ResultKey {

    private final ITestResult result;

    ResultKey(ITestResult result) {
      this.result = result;
    }

    @Override
    public boolean equals(@Nullable Object other) {
      return other instanceof ResultKey && ((ResultKey) other).result == result;
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(result);
    }
  }
}
