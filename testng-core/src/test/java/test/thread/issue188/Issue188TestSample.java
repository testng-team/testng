package test.thread.issue188;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Issue188TestSample {
  public static final int EXPECTED_METHOD_INVOCATIONS = 6;

  /** Guard against deadlock if the methods are (wrongly) serialized instead of running together. */
  private static final long BARRIER_TIMEOUT_SECONDS = 10;

  private static final AtomicInteger startedTestMethods = new AtomicInteger();
  private static final AtomicInteger activeTestMethods = new AtomicInteger();
  private static final AtomicInteger maxActiveTestMethods = new AtomicInteger();
  private static CountDownLatch allMethodsActive = new CountDownLatch(EXPECTED_METHOD_INVOCATIONS);

  public static void reset() {
    startedTestMethods.set(0);
    activeTestMethods.set(0);
    maxActiveTestMethods.set(0);
    allMethodsActive = new CountDownLatch(EXPECTED_METHOD_INVOCATIONS);
  }

  public static int getStartedTestMethods() {
    return startedTestMethods.get();
  }

  public static int getMaxActiveTestMethods() {
    return maxActiveTestMethods.get();
  }

  @BeforeMethod
  public void logStart() {
    startedTestMethods.incrementAndGet();
  }

  @Test
  public void sampleTest() {
    trackParallelExecution();
  }

  @Test
  public void anotherSampleTest() {
    trackParallelExecution();
  }

  private static void trackParallelExecution() {
    int activeMethods = activeTestMethods.incrementAndGet();
    maxActiveTestMethods.accumulateAndGet(activeMethods, Math::max);
    allMethodsActive.countDown();
    try {
      // Hold the active slot until every expected method has reached this barrier rather than
      // sleeping for a fixed window: the peak active count then reaches EXPECTED_METHOD_INVOCATIONS
      // deterministically when the methods run in parallel, instead of relying on a timing margin.
      allMethodsActive.await(BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      activeTestMethods.decrementAndGet();
    }
  }
}
