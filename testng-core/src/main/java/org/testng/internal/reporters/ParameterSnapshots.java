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
 * #requestCapture()}, or with {@link #requestCaptureHeldUntilReporting()} when it will read the
 * store after the invocations are over -- which is also what decides whether anything may be
 * dropped along the way. See {@link #discard}.
 */
public final class ParameterSnapshots {

  /**
   * Mirrors {@code IObjectDispenser.GUICE_HELPER}: an internal object parked on a TestNG attribute
   * under a name of TestNG's own.
   */
  private static final String ATTRIBUTE = "testng.reporting-parameter-snapshots";

  private final Map<ResultKey, ParameterSnapshot> snapshots = new ConcurrentHashMap<>();
  private volatile boolean captureRequested;
  private volatile boolean heldUntilReporting;

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

  /**
   * Declares that a reporter will read these snapshots, so they are worth taking, and that it can
   * live with {@link #discard} dropping a configuration method that passed.
   *
   * <p>Which is not the same as reading from inside the invocation lifecycle, tempting as that
   * shorthand is: {@code TextReporter} reads at {@code onFinish}, once every configuration of its
   * context has already succeeded. What makes both callers of this safe is narrower -- neither
   * lists {@link org.testng.ITestContext#getPassedConfigurations()}, which is the only thing {@code
   * discard} is ever offered. A reporter that starts listing them needs {@link
   * #requestCaptureHeldUntilReporting()} instead, whenever it reads.
   */
  public void requestCapture() {
    captureRequested = true;
  }

  /**
   * The same, for a reporter that reads the store once the invocations of the run are over: nothing
   * is dropped for such a run, since a snapshot the live reporters are finished with is one it has
   * not seen yet.
   *
   * <p>Kept as a second flag rather than as one ordered value on purpose. Both are monotone writes
   * of {@code true} and neither clears the other, so a run that has both kinds of reader -- the
   * default one above {@code -verbose 4} -- gets the same answer whichever asks first. Folding them
   * into one field updated to a maximum would turn two race-free writes into a read-modify-write,
   * and {@code <suite parallel="tests">} makes those calls from two runners at once.
   */
  public void requestCaptureHeldUntilReporting() {
    captureRequested = true;
    heldUntilReporting = true;
  }

  /**
   * The {@link #requestCapture()} of a reporter that has a suite rather than a store: a suite
   * without one is a suite whose snapshots nobody is going to take, which is not the reporter's
   * business.
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
   * The same, for a reporter that will read the store once the invocations of the run are over. It
   * has no invocation lifecycle to ask from, so the run asks on its behalf; see {@link
   * ParameterSnapshotReader#requestCaptureIfAnyReads}.
   *
   * @param suite - A suite about to run, none of whose invocations has started.
   */
  public static void requestCaptureHeldUntilReportingFor(@Nullable ISuite suite) {
    ParameterSnapshots snapshots = of(suite);
    if (snapshots != null) {
      snapshots.requestCaptureHeldUntilReporting();
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
   * Offers back what was captured for a result every live reporter is done with -- a configuration
   * method that succeeded, which each of them prints as it passes and none of them lists again.
   *
   * <p>Half of that decision is the caller's and half is not, which is why it is taken here. The
   * caller knows the reporters that sit in the invocation lifecycle have had it; only the store
   * knows whether a reporter that has not run yet still wants it, and one that reads at {@code
   * generateReport} does: {@code testng-results.xml} lists the configurations that passed. So a run
   * that asked through {@link #requestCaptureHeldUntilReporting()} keeps everything until {@link
   * #detachFrom} releases it, and a run whose readers are all live ones drops as it goes.
   *
   * <p>Guarded like {@link #captureIfAbsent} for the same reason: {@code @BeforeMethod} /
   * {@code @AfterMethod} invocations are the most frequent events in a run, and there is nothing to
   * drop when nothing is being captured.
   *
   * @param result - The result the live reporters have finished with.
   */
  public void discard(ITestResult result) {
    if (!discardsWhatIsDone()) {
      return;
    }
    snapshots.remove(new ResultKey(result));
  }

  /** Named so that {@code discard} not discarding does not read as a bug. */
  private boolean discardsWhatIsDone() {
    return captureRequested && !heldUntilReporting;
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

    // Identity on purpose, and the reason this wrapper exists: the key is one particular result,
    // not a result that looks like it. hashCode below is the identity hash for the same reason.
    @SuppressWarnings("ReferenceEquality")
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
