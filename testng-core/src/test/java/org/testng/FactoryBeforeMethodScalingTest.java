package org.testng;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * A {@code @Factory} that declares {@code @BeforeMethod} used to scan every instance on each
 * invocation. Four times the instances must not cost about sixteen times the run.
 */
public class FactoryBeforeMethodScalingTest {

  private static final int SMALL = 400;
  private static final int LARGE = 1600;
  private static final int ATTEMPTS = 3;
  private static final AtomicInteger SETUPS = new AtomicInteger();
  private static final AtomicInteger TEARDOWNS = new AtomicInteger();

  @Test
  public void methodConfigurationStaysLinearAsFactoryGrows() {
    long small = fastest(SMALL);
    long large = fastest(LARGE);
    double perSmall = (double) small / SMALL;
    double perLarge = (double) large / LARGE;
    // Before the index, 400 vs 1600 instances cost 1.58x per instance (6.32x wall time).
    // A linear run keeps the per-instance cost flat, or lower once fixed overhead amortises.
    assertThat(perLarge)
        .as("small=%s large=%s per=%.3f/%.3f", small, large, perSmall, perLarge)
        .isLessThan(perSmall * 1.35);
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
