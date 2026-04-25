package test.thread.issue188;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Issue188TestSample {
  public static final int EXPECTED_METHOD_INVOCATIONS = 6;
  private static final AtomicInteger startedTestMethods = new AtomicInteger();
  private static final AtomicInteger activeTestMethods = new AtomicInteger();
  private static final AtomicInteger maxActiveTestMethods = new AtomicInteger();

  public static void reset() {
    startedTestMethods.set(0);
    activeTestMethods.set(0);
    maxActiveTestMethods.set(0);
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
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      activeTestMethods.decrementAndGet();
    }
  }
}
