package test.factory.lazy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Lazy;
import org.testng.annotations.Test;

/**
 * A constructor factory that explicitly opts into lazy instantiation via the annotation. Records
 * how many instances existed when each test ran (see {@link CountingFactorySample} for the
 * convention): lazy yields {@code [1, 2, 3, 4]}, eager would yield {@code [4, 4, 4, 4]}.
 */
public class AnnotationLazyTrueSample {

  public static final AtomicInteger CONSTRUCTED = new AtomicInteger();

  public static final List<Integer> INSTANCES_ALIVE_WHEN_EACH_TEST_RAN =
      new CopyOnWriteArrayList<>();

  @Factory(dataProvider = "indices", lazy = Lazy.TRUE)
  public AnnotationLazyTrueSample(int index) {
    CONSTRUCTED.incrementAndGet();
  }

  @DataProvider
  public static Object[][] indices() {
    return new Object[][] {{0}, {1}, {2}, {3}};
  }

  @Test
  public void test() {
    INSTANCES_ALIVE_WHEN_EACH_TEST_RAN.add(CONSTRUCTED.get());
  }

  public static void reset() {
    CONSTRUCTED.set(0);
    INSTANCES_ALIVE_WHEN_EACH_TEST_RAN.clear();
  }
}
