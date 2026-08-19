package test.configuration.issue3358;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ChildFirstTimeOnlyParallelDpFailingSample extends ParentFirstTimeOnlySample {
  public static final AtomicInteger testBodies = new AtomicInteger();

  public static void reset() {
    testBodies.set(0);
  }

  @BeforeMethod(firstTimeOnly = true)
  public void beforeChild() {
    try {
      Thread.sleep(40);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    throw new RuntimeException("child firstTimeOnly failed");
  }

  @DataProvider(name = "rows", parallel = true)
  public Object[][] rows() {
    return new Object[][] {{0}, {1}, {2}, {3}, {4}};
  }

  @Test(dataProvider = "rows")
  public void test(int n) {
    testBodies.incrementAndGet();
  }
}
