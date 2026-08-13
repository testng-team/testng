package test.factory.lazy;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * A constructor factory (4 instances) that also declares a {@code @BeforeSuite} configuration
 * method, used to verify that a suite level configuration on a factory-powered class behaves
 * correctly — and does not defeat lazy instantiation — when lazy is enabled. Instrumentation
 * follows the same convention as {@link CountingFactorySample}.
 */
public class BeforeSuiteFactorySample {

  public static final AtomicInteger CONSTRUCTED = new AtomicInteger();

  public static final AtomicInteger BEFORE_SUITE_FIRED = new AtomicInteger();

  public static final List<Integer> INSTANCES_ALIVE_WHEN_EACH_TEST_RAN =
      new CopyOnWriteArrayList<>();

  public static final Set<Integer> TESTS_RUN = ConcurrentHashMap.newKeySet();

  private final int index;

  @Factory(dataProvider = "indices")
  public BeforeSuiteFactorySample(int index) {
    this.index = index;
    CONSTRUCTED.incrementAndGet();
  }

  @DataProvider
  public static Object[][] indices() {
    return new Object[][] {{0}, {1}, {2}, {3}};
  }

  @BeforeSuite
  public void beforeSuite() {
    BEFORE_SUITE_FIRED.incrementAndGet();
  }

  @Test
  public void test() {
    INSTANCES_ALIVE_WHEN_EACH_TEST_RAN.add(CONSTRUCTED.get());
    TESTS_RUN.add(index);
  }

  public static void reset() {
    CONSTRUCTED.set(0);
    BEFORE_SUITE_FIRED.set(0);
    INSTANCES_ALIVE_WHEN_EACH_TEST_RAN.clear();
    TESTS_RUN.clear();
  }
}
