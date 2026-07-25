package test.dataprovider.issue3242;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A sample that mixes regular (non data-driven) test methods with data-driven ones, so the tests
 * can assert that both kinds run to completion on a single shared global thread-pool. Each
 * invocation pauses briefly so that an attached {@link ConcurrencyProbe} can observe the
 * concurrency.
 */
public class MixedThreadPoolSample {

  @Test
  public void regular1() {
    pause();
  }

  @Test
  public void regular2() {
    pause();
  }

  @Test(dataProvider = "data")
  public void dataDriven1(int ignored) {
    pause();
  }

  @Test(dataProvider = "data")
  public void dataDriven2(int ignored) {
    pause();
  }

  @DataProvider(name = "data", parallel = true)
  public Object[][] data() {
    return new Object[][] {{1}, {2}, {3}, {4}};
  }

  private static void pause() {
    try {
      Thread.sleep(150);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
