package org.testng.timeout.samples.issue2091;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Runs two rows of one timed method. The first row overruns its time-out and ignores the
 * interruption, so it still runs when the second row starts. Each row records its thread.
 */
public class OverlappingRowsSample {

  private static volatile @Nullable Thread firstRowThread;
  private static volatile @Nullable Thread secondRowThread;
  private static volatile boolean firstRowAliveWhenSecondStarted;
  private static volatile CountDownLatch secondRowDone = new CountDownLatch(1);

  public static void reset() {
    firstRowThread = null;
    secondRowThread = null;
    firstRowAliveWhenSecondStarted = false;
    secondRowDone = new CountDownLatch(1);
  }

  public static @Nullable Thread firstRowThread() {
    return firstRowThread;
  }

  public static @Nullable Thread secondRowThread() {
    return secondRowThread;
  }

  public static boolean firstRowAliveWhenSecondStarted() {
    return firstRowAliveWhenSecondStarted;
  }

  @DataProvider
  public Object[][] rows() {
    return new Object[][] {{0}, {1}};
  }

  @Test(timeOut = 300, dataProvider = "rows")
  public void timed(int row) {
    Thread current = Thread.currentThread();
    if (row == 0) {
      firstRowThread = current;
      waitForSecondRow();
    } else {
      secondRowThread = current;
      Thread first = firstRowThread;
      firstRowAliveWhenSecondStarted = first != null && first.isAlive();
      secondRowDone.countDown();
    }
  }

  // Ignores interruption, as the method in GITHUB-2091 does, but gives up after 5 seconds.
  private static void waitForSecondRow() {
    long end = System.currentTimeMillis() + 5_000;
    while (true) {
      long left = end - System.currentTimeMillis();
      if (left <= 0) {
        return;
      }
      try {
        if (secondRowDone.await(left, TimeUnit.MILLISECONDS)) {
          return;
        }
      } catch (InterruptedException ignored) {
        // Keep going.
      }
    }
  }
}
