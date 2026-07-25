package test.dataprovider.issue3242;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

/**
 * An {@link IInvokedMethodListener} that measures how a single TestNG run exercised its
 * thread-pool: the total number of test invocations, the highest number that ran concurrently, and
 * the distinct threads that were used.
 *
 * <p>A fresh instance is created per test and attached to that test's own (nested) TestNG instance,
 * so it holds no shared static state - it stays correct even if these unit tests are themselves run
 * in parallel. Each invocation is bracketed by {@link #beforeInvocation}/{@link #afterInvocation}
 * on the same thread that runs the test method, which is why the samples pause briefly: it lets the
 * invocations overlap so the concurrency can actually be observed.
 */
public final class ConcurrencyProbe implements IInvokedMethodListener {

  private final AtomicInteger active = new AtomicInteger();
  private final AtomicInteger maxConcurrency = new AtomicInteger();
  private final AtomicInteger invocations = new AtomicInteger();
  private final Set<Long> threadIds = ConcurrentHashMap.newKeySet();

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    if (!method.isTestMethod()) {
      return;
    }
    invocations.incrementAndGet();
    threadIds.add(Thread.currentThread().getId());
    int current = active.incrementAndGet();
    maxConcurrency.accumulateAndGet(current, Math::max);
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    if (method.isTestMethod()) {
      active.decrementAndGet();
    }
  }

  /** The total number of test invocations that were executed. */
  public int invocations() {
    return invocations.get();
  }

  /** The highest number of test invocations that were observed running concurrently. */
  public int maxConcurrency() {
    return maxConcurrency.get();
  }

  /** The number of distinct threads that ran the test invocations. */
  public int distinctThreadsUsed() {
    return threadIds.size();
  }
}
