package test.factory.lazy;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

/**
 * Demonstrates <em>when</em> factory instances are constructed, by comparing two observations of
 * the same counter ({@link CountingFactorySample#CONSTRUCTED}):
 *
 * <ol>
 *   <li><b>at the start of the run</b> — captured by {@link InstancesConstructedAtSuiteStart}, a
 *       suite listener whose {@code onStart} fires after collection but before any test. A listener
 *       is required because {@link TestNG#run()} is synchronous: the test thread only regains
 *       control once everything has finished, so the only way to observe the mid-run "the run just
 *       started" moment is a callback that fires at that moment. (If the callback never fired, its
 *       captured value stays {@code -1} and the assertions below fail — so the timing is not taken
 *       on faith.)
 *   <li><b>after the run</b> — read directly by the test from the counter.
 * </ol>
 *
 * The two readings tell the whole story: eager is {@code 4 -> 4} (built before the run), lazy is
 * {@code 0 -> 4} (built during the run, on demand).
 */
public class LazyFactoryConstructionTest extends SimpleBaseTest {

  @Test
  public void eagerByDefaultBuildsEveryInstanceBeforeTheRunStarts() {
    CountingFactorySample.reset();
    TestNG tng = create(CountingFactorySample.class);
    tng.setPreserveOrder(true);
    InstancesConstructedAtSuiteStart atSuiteStart =
        new InstancesConstructedAtSuiteStart(CountingFactorySample.CONSTRUCTED::get);
    tng.addListener(atSuiteStart);
    tng.run();

    // Snapshot taken by the listener when the run started vs. the counter read here after the run.
    // Eager: all four already existed before the run, and the count never moved (4 -> 4).
    assertThat(atSuiteStart.countAtStart())
        .as("instances built before the run started")
        .isEqualTo(4);
    assertThat(CountingFactorySample.CONSTRUCTED.get())
        .as("instances built by the end")
        .isEqualTo(4);

    // Consequently every test saw all four instances.
    assertThat(CountingFactorySample.INSTANCES_ALIVE_WHEN_EACH_TEST_RAN)
        .containsExactly(4, 4, 4, 4);
  }

  @Test
  public void lazyDefersConstructionUntilEachTestRuns() {
    CountingFactorySample.reset();
    TestNG tng = create(CountingFactorySample.class);
    tng.setPreserveOrder(true);
    tng.setLazyFactoryInstantiation(true);
    InstancesConstructedAtSuiteStart atSuiteStart =
        new InstancesConstructedAtSuiteStart(CountingFactorySample.CONSTRUCTED::get);
    tng.addListener(atSuiteStart);
    tng.run();

    // Same two observations. Lazy: nothing existed when the run started, yet four exist by the end
    // —
    // so all four were constructed DURING the run (0 -> 4), not before it.
    assertThat(atSuiteStart.countAtStart()).as("instances built before the run started").isZero();
    assertThat(CountingFactorySample.CONSTRUCTED.get())
        .as("instances built by the end")
        .isEqualTo(4);

    // And they arrived one at a time, each just before its own test.
    assertThat(CountingFactorySample.INSTANCES_ALIVE_WHEN_EACH_TEST_RAN)
        .containsExactly(1, 2, 3, 4);
  }
}
