package org.testng;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * A {@code @Factory} that declares {@code @BeforeMethod} used to scan every instance on each
 * invocation. Each instance must still run its hooks once per test method. On a normal JVM a larger
 * factory must not cost more than twice as much per instance. {@code -XX:hashCode=2} degenerates
 * {@code HashMap} across the engine, so that job only checks the hook counts.
 */
public class FactoryBeforeMethodScalingTest {

  private static final int SMALL = 400;
  private static final int LARGE = 1600;
  private static final int ATTEMPTS = 3;
  /** Local pre-fix was 1.58x. Shared runners do not hold 1.35 (one quiet job landed at 1.39). */
  private static final double MAX_PER_INSTANCE_GROWTH = 2.0;

  private static final AtomicInteger SETUPS = new AtomicInteger();
  private static final AtomicInteger TEARDOWNS = new AtomicInteger();

  @Test
  public void methodConfigurationStaysLinearAsFactoryGrows() {
    if (identityHashCodesCollide()) {
      run(SMALL);
      return;
    }
    long small = fastest(SMALL);
    long large = fastest(LARGE);
    double perSmall = (double) small / SMALL;
    double perLarge = (double) large / LARGE;
    // 2.0 still rejects a quadratic 4x. The index itself is pinned in
    // TestClassConfigurationLookupTest.
    assertThat(perLarge)
        .as("small=%s large=%s per=%.3f/%.3f", small, large, perSmall, perLarge)
        .isLessThan(perSmall * MAX_PER_INSTANCE_GROWTH);
  }

  /** HotSpot {@code -XX:hashCode=2} gives every object the same identity hash code. */
  private static boolean identityHashCodesCollide() {
    Object left = new Object();
    Object right = new Object();
    return System.identityHashCode(left) == System.identityHashCode(right);
  }

  private static long fastest(int instances) {
    long best = Long.MAX_VALUE;
    for (int i = 0; i < ATTEMPTS; i++) {
      best = Math.min(best, run(instances));
    }
    return best;
  }

  private static long run(int instances) {
    TestNG testng = new TestNG();
    testng.setUseDefaultListeners(false);
    testng.setVerbose(0);
    Class<?> factory = instances == SMALL ? SmallFactory.class : LargeFactory.class;
    testng.setTestClasses(new Class[] {factory});
    SETUPS.set(0);
    TEARDOWNS.set(0);
    long started = System.nanoTime();
    testng.run();
    long elapsed = System.nanoTime() - started;
    assertThat(testng.getStatus()).isZero();
    assertThat(SETUPS.get()).isEqualTo(instances * 2);
    assertThat(TEARDOWNS.get()).isEqualTo(instances * 2);
    return elapsed;
  }

  public static class Bean {
    @BeforeMethod
    public void setUp() {
      SETUPS.incrementAndGet();
    }

    @AfterMethod
    public void tearDown() {
      TEARDOWNS.incrementAndGet();
    }

    @Test
    public void one() {}

    @Test
    public void two() {}
  }

  public static class SmallFactory {
    @Factory
    public Object[] create() {
      return copies(SMALL);
    }
  }

  public static class LargeFactory {
    @Factory
    public Object[] create() {
      return copies(LARGE);
    }
  }

  private static Object[] copies(int count) {
    Bean[] beans = new Bean[count];
    for (int i = 0; i < count; i++) {
      beans[i] = new Bean();
    }
    return beans;
  }
}
