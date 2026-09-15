package org.testng.conffailure.samples.retry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Two rows on two threads. Row 2's setup fails and marks the class while row 1's first attempt is
 * inside its test method; row 1 then fails and is retried. That mark was recorded before the retry
 * started, but by another row, so the retry must honor it.
 *
 * <p>Two latches fix the order. Row 2's setup waits for row 1's test method to have started, so the
 * mark cannot land between row 1's setup and its own failure check, which would skip row 1 outright
 * and leave nothing to retry. Row 1's test method then waits for row 2's setup to have failed
 * before it fails itself.
 */
public class ParallelRowsSample {

  private final CountDownLatch row1Started = new CountDownLatch(1);
  private final CountDownLatch row2SetupFailed = new CountDownLatch(1);
  private volatile boolean row1Failed;

  @DataProvider(parallel = true)
  public Object[][] rows() {
    return new Object[][] {{1}, {2}};
  }

  @BeforeMethod
  public void setup(Object[] params) throws InterruptedException {
    if ((Integer) params[0] == 2) {
      assertThat(row1Started.await(10, TimeUnit.SECONDS))
          .withFailMessage("row 1 did not start within 10s")
          .isTrue();
      row2SetupFailed.countDown();
      throw new IllegalStateException("setup fails for row 2");
    }
  }

  @Test(dataProvider = "rows", retryAnalyzer = RetryOnce.class)
  public void t(int row) throws InterruptedException {
    if (row == 1 && !row1Failed) {
      row1Failed = true;
      row1Started.countDown();
      assertThat(row2SetupFailed.await(10, TimeUnit.SECONDS))
          .withFailMessage("row 2's setup did not fail within 10s")
          .isTrue();
      throw new AssertionError("row 1 fails on its first attempt only");
    }
  }
}
