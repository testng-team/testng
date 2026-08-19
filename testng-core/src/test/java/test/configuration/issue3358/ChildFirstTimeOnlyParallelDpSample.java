package test.configuration.issue3358;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ChildFirstTimeOnlyParallelDpSample extends ParentFirstTimeOnlySample {
  public static final AtomicLong childFinishedAt = new AtomicLong();
  public static final List<Long> testStartedAt = new CopyOnWriteArrayList<>();

  public static void reset() {
    childFinishedAt.set(0);
    testStartedAt.clear();
  }

  @BeforeMethod(firstTimeOnly = true)
  public void beforeChild() {
    // Widen the race: parallel data-provider workers share the same ITestNGMethod and
    // all see getCurrentInvocationCount() == 0 until the first invocation finishes.
    try {
      Thread.sleep(40);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    childFinishedAt.set(System.nanoTime());
  }

  @DataProvider(name = "rows", parallel = true)
  public Object[][] rows() {
    return new Object[][] {{0}, {1}, {2}, {3}, {4}};
  }

  @Test(dataProvider = "rows")
  public void test(int n) {
    testStartedAt.add(System.nanoTime());
  }
}
