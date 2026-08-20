package org.testng.reporters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import org.testng.ITestContext;
import org.testng.ITestResult;

/**
 * The {@link ParameterSnapshot} a reporter has captured, one per invocation it was told about.
 *
 * <p>Reporting owns this: it is filled from the invocation lifecycle a reporter already listens to,
 * and it is disposable. A result whose snapshot is missing is not an error -- the reporter falls
 * back to {@link ITestResult#getParameters()}, which is what it read before.
 *
 * <p>Results are held by identity. {@link ITestResult} is a public interface, so an implementation
 * is free to define {@code equals()} in a way that makes two invocations of the same method with
 * the same values indistinguishable, and reporting must not let that merge their snapshots.
 *
 * <p>One store per reporter is the shape of this slice, which migrates a single reporter. It is not
 * the shape the rest of them should be migrated with: a store each would render every value once
 * per reporter, running the user's {@code toString()} N times for one piece of information. The
 * capture belongs upstream of the reporters, once per invocation and shared. Note for whoever
 * writes it that {@link org.testng.IReporter} implementations -- {@code EmailableReporter2}, {@code
 * XMLReporter}, {@code jq.Main} -- run after every context has finished, so a shared store cannot
 * be discarded on {@code onFinish} the way this one is.
 */
final class ParameterSnapshots {

  private final Map<ResultKey, ParameterSnapshot> snapshots = new ConcurrentHashMap<>();

  /**
   * Captures what {@code result} was invoked with, if there is anything worth keeping. Must be
   * called from a lifecycle point the invocation has not run past yet.
   *
   * @param result - The result an invocation is starting with.
   */
  void capture(ITestResult result) {
    ParameterSnapshot snapshot;
    try {
      snapshot =
          ParameterSnapshot.of(result.getParameters(), result.getMethod().getParameterTypes());
    } catch (Throwable rendering) {
      // Rendering a value calls the user's toString(). One that throws would otherwise fail the
      // invocation it is only being reported on; leaving the result unsnapshotted hands it back to
      // ITestResult#getParameters(), where such a value has always thrown at reporting time.
      return;
    }
    if (snapshot != null) {
      snapshots.put(new ResultKey(result), snapshot);
    }
  }

  /**
   * @param result - The result being reported.
   * @return - What was captured for it, or {@code null} if nothing was.
   */
  @Nullable
  ParameterSnapshot find(ITestResult result) {
    return snapshots.get(new ResultKey(result));
  }

  /**
   * Drops what was captured for a result a reporter now knows it will never print. A configuration
   * method that succeeded is the case that matters: it is announced like any other, but no reporter
   * lists it, so its snapshot would otherwise sit here until the whole context is done.
   *
   * @param result - The result that will not be reported.
   */
  void discard(ITestResult result) {
    snapshots.remove(new ResultKey(result));
  }

  /**
   * Drops what was captured for a context that has finished reporting. Snapshots taken for the
   * other contexts of a parallel run are left alone, since a reporter registered on the suite sees
   * them all.
   *
   * @param context - The context whose results have been reported.
   */
  void discard(ITestContext context) {
    snapshots.keySet().removeIf(key -> key.belongsTo(context));
  }

  private static final class ResultKey {

    private final ITestResult result;

    ResultKey(ITestResult result) {
      this.result = result;
    }

    boolean belongsTo(ITestContext context) {
      return result.getTestContext() == context;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof ResultKey && ((ResultKey) other).result == result;
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(result);
    }
  }
}
