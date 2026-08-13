package test.factory.lazy;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * A constructor based, data-provider driven {@code @Factory} (4 instances) instrumented to make
 * lazy vs eager instantiation directly observable — without a reader having to decode any event
 * log:
 *
 * <ul>
 *   <li>{@link #CONSTRUCTED} — the running total of instances created so far.
 *   <li>{@link #INSTANCES_ALIVE_WHEN_EACH_TEST_RAN} — for each test, in run order, how many
 *       instances had been constructed by the time that test ran. Eager yields {@code [4, 4, 4, 4]}
 *       (all instances exist before any test); lazy yields {@code [1, 2, 3, 4]} (each instance is
 *       created just before its own test).
 *   <li>{@link #TESTS_RUN} — the set of instance indices whose test ran (order-independent, for
 *       parallel checks).
 * </ul>
 *
 * <p>The {@code lazy} attribute is intentionally left unset so tests can drive the instantiation
 * timing through each opt-in surface (annotation, suite XML, TestNG configuration).
 */
public class CountingFactorySample {

  public static final AtomicInteger CONSTRUCTED = new AtomicInteger();

  public static final List<Integer> INSTANCES_ALIVE_WHEN_EACH_TEST_RAN =
      new CopyOnWriteArrayList<>();

  public static final Set<Integer> TESTS_RUN = ConcurrentHashMap.newKeySet();

  private final int index;

  @Factory(dataProvider = "indices")
  public CountingFactorySample(int index) {
    this.index = index;
    CONSTRUCTED.incrementAndGet();
  }

  @DataProvider
  public static Object[][] indices() {
    return new Object[][] {{0}, {1}, {2}, {3}};
  }

  @Test
  public void test() {
    INSTANCES_ALIVE_WHEN_EACH_TEST_RAN.add(CONSTRUCTED.get());
    TESTS_RUN.add(index);
  }

  public static void reset() {
    CONSTRUCTED.set(0);
    INSTANCES_ALIVE_WHEN_EACH_TEST_RAN.clear();
    TESTS_RUN.clear();
  }
}
