package org.testng.conffailure.samples.retry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Two rows on two threads, one instance. Row 2's setup fails and records a class failure. Row 1
 * then fails its test and its alwaysRun teardown, and that second record replaces the first when
 * the map holds only one failure per instance.
 *
 * <p>Row 1's retry takes its mark after its own teardown. The replacement belongs to that thread
 * and predates the mark, so it is ignored. The retry must still see row 2's record and stay
 * skipped: a later failure for the same instance must not erase what another worker recorded.
 *
 * <p>The latches fix the order of the records, not only of the throws. Row 2's setup waits for row
 * 1's test method to have started, so row 1 is not skipped before it has something to retry. Row
 * 2's alwaysRun teardown then signals that the setup failure has been recorded. Row 1's test method
 * and teardown wait for that signal, so the first record is in place when the second is written.
 */
public class OverwrittenFailureSample {

  private final CountDownLatch row1Started = new CountDownLatch(1);
  private final CountDownLatch row2FailureRecorded = new CountDownLatch(1);
  private final AtomicInteger row1Attempts = new AtomicInteger();

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
      throw new IllegalStateException("setup fails for row 2");
    }
  }

  @Test(dataProvider = "rows", retryAnalyzer = RetryOnce.class)
  public void t(int row) throws InterruptedException {
    if (row == 1 && row1Attempts.incrementAndGet() == 1) {
      row1Started.countDown();
      assertThat(row2FailureRecorded.await(10, TimeUnit.SECONDS))
          .withFailMessage("row 2's setup failure was not recorded within 10s")
          .isTrue();
      throw new AssertionError("row 1 fails on its first attempt only");
    }
  }

  @AfterMethod(alwaysRun = true)
  public void teardown(Object[] params) throws InterruptedException {
    int row = (Integer) params[0];
    if (row == 2) {
      row2FailureRecorded.countDown();
      return;
    }
    if (row == 1 && row1Attempts.get() == 1) {
      assertThat(row2FailureRecorded.await(10, TimeUnit.SECONDS))
          .withFailMessage("row 2's setup failure was not recorded within 10s")
          .isTrue();
      throw new IllegalStateException("teardown fails after the first attempt of row 1");
    }
  }
}
