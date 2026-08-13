package test.factory.lazy;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Lazy;
import org.testng.annotations.Test;

/**
 * A lazy constructor factory where exactly one instance (index {@code 2}) fails to construct. Used
 * to verify that a lazy constructor failure is localized to that instance's test methods while the
 * other instances still run.
 */
public class ThrowingLazyFactorySample {

  /** The indices whose test actually ran (the failing instance's test never does). */
  public static final Set<Integer> TESTS_RUN = ConcurrentHashMap.newKeySet();

  private final int index;

  @Factory(dataProvider = "indices", lazy = Lazy.TRUE)
  public ThrowingLazyFactorySample(int index) {
    if (index == 2) {
      throw new IllegalStateException("boom for index 2");
    }
    this.index = index;
  }

  @DataProvider
  public static Object[][] indices() {
    return new Object[][] {{0}, {1}, {2}, {3}};
  }

  @Test
  public void test() {
    TESTS_RUN.add(index);
  }

  public static void reset() {
    TESTS_RUN.clear();
  }
}
