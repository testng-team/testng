package org.testng.configuration.issue3435;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

/** GITHUB-3435. Counts, not a clock: each factory instance keeps its own method configuration. */
public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3435")
  public void beforeAndAfterMethodStayScopedToEachFactoryInstance() {
    ScopedFactory.reset();
    TestNG testng = create(ScopedFactory.class);
    testng.run();

    assertThat(testng.getStatus()).isZero();
    assertThat(ScopedFactory.counts).hasSize(3);
    assertThat(ScopedFactory.counts.values())
        .allMatch(counts -> counts.before.get() == 2 && counts.after.get() == 2);
  }

  @Test(description = "GITHUB-3435")
  public void pooledFirstAndLastTimeOnlyStayScopedToEachFactoryInstance() {
    PooledTimeOnlyFactory.reset();
    TestNG testng = create(PooledTimeOnlyFactory.class);
    testng.run();

    assertThat(testng.getStatus()).isZero();
    assertThat(PooledTimeOnlyFactory.counts).hasSize(2);
    assertThat(PooledTimeOnlyFactory.counts.values())
        .as(
            "counts=%s",
            PooledTimeOnlyFactory.counts.values().stream()
                .map(
                    counts ->
                        counts.before.get() + "/" + counts.after.get() + "/" + counts.tests.get())
                .collect(Collectors.toList()))
        .allMatch(
            counts ->
                counts.before.get() == 1 && counts.after.get() == 1 && counts.tests.get() == 3);
  }

  public static class ScopedFactory {
    static final ConcurrentHashMap<Object, HookCounts> counts = new ConcurrentHashMap<>();

    static void reset() {
      counts.clear();
    }

    @Factory
    public static Object[] instances() {
      return new Object[] {new ScopedFactory(), new ScopedFactory(), new ScopedFactory()};
    }

    @BeforeMethod
    public void setUp() {
      counts.computeIfAbsent(this, key -> new HookCounts()).before.incrementAndGet();
    }

    @AfterMethod
    public void tearDown() {
      counts.computeIfAbsent(this, key -> new HookCounts()).after.incrementAndGet();
    }

    @Test
    public void one() {}

    @Test
    public void two() {}
  }

  public static class PooledTimeOnlyFactory {
    static final ConcurrentHashMap<Object, HookCounts> counts = new ConcurrentHashMap<>();

    static void reset() {
      counts.clear();
    }

    @Factory
    public static Object[] instances() {
      return new Object[] {new PooledTimeOnlyFactory(), new PooledTimeOnlyFactory()};
    }

    @BeforeMethod(firstTimeOnly = true)
    public void beforeFirst() {
      counts.computeIfAbsent(this, key -> new HookCounts()).before.incrementAndGet();
    }

    @AfterMethod(lastTimeOnly = true)
    public void afterLast() {
      counts.computeIfAbsent(this, key -> new HookCounts()).after.incrementAndGet();
    }

    @Test(invocationCount = 3, threadPoolSize = 2)
    public void test() {
      counts.computeIfAbsent(this, key -> new HookCounts()).tests.incrementAndGet();
    }
  }

  static final class HookCounts {
    final AtomicInteger before = new AtomicInteger();
    final AtomicInteger after = new AtomicInteger();
    final AtomicInteger tests = new AtomicInteger();
  }
}
