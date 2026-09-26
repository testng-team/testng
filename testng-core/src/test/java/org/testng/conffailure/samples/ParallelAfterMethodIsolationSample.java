package org.testng.conffailure.samples;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Two parallel data-provider rows. Row 0's teardown fails. Row 1 waits until that failure is
 * recorded, then must still run. The latch is released by the test, from the seam that runs after
 * the record: a listener fires before it.
 */
public class ParallelAfterMethodIsolationSample {

  public static CountDownLatch tornDown;

  public static void reset() {
    tornDown = new CountDownLatch(1);
  }

  @DataProvider(name = "rows", parallel = true)
  public Object[][] rows() {
    return new Object[][] {{0}, {1}};
  }

  @BeforeMethod
  public void setup(Object[] params) throws InterruptedException {
    if ((Integer) params[0] == 1) {
      assertThat(tornDown.await(10, TimeUnit.SECONDS))
          .withFailMessage("row 0 did not record its teardown failure within 10s")
          .isTrue();
    }
  }

  @Test(dataProvider = "rows")
  public void t(int row) {}

  @AfterMethod
  public void teardown(Object[] params) {
    if ((Integer) params[0] == 0) {
      throw new IllegalStateException("teardown fails for row 0");
    }
  }
}
