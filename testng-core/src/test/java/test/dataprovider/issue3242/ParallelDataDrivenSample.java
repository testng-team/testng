package test.dataprovider.issue3242;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A sample that mimics the scenario reported in GITHUB-3242: a class with several data-driven test
 * methods, each backed by a parallel data-provider. Each invocation pauses briefly so that a {@link
 * ConcurrencyProbe} attached to the run can observe how the shared global thread-pool is used.
 */
public class ParallelDataDrivenSample {

  @Test(dataProvider = "data")
  public void test1(int ignored) {
    pause();
  }

  @Test(dataProvider = "data")
  public void test2(int ignored) {
    pause();
  }

  @Test(dataProvider = "data")
  public void test3(int ignored) {
    pause();
  }

  @Test(dataProvider = "data")
  public void test4(int ignored) {
    pause();
  }

  @Test(dataProvider = "data")
  public void test5(int ignored) {
    pause();
  }

  @DataProvider(name = "data", parallel = true)
  public Object[][] data() {
    return new Object[][] {{1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}};
  }

  private static void pause() {
    try {
      Thread.sleep(150);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
