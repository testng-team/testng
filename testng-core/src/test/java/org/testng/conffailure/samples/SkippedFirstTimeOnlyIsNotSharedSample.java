package org.testng.conffailure.samples;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.testng.IConfigurationListener;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Row 0 fails its own setup. The later {@code firstTimeOnly} setup is skipped, not failed. That
 * skip must not become a shared failure, or row 1 is skipped under {@code CONTINUE}.
 *
 * <p>Row 1 waits until that skip is recorded, so the shared-token lookup cannot race ahead of it.
 */
public class SkippedFirstTimeOnlyIsNotSharedSample {

  public static CountDownLatch sharedSkipped;

  public static void reset() {
    sharedSkipped = new CountDownLatch(1);
  }

  @DataProvider(name = "rows", parallel = true)
  public Object[][] rows() {
    return new Object[][] {{0}, {1}};
  }

  @BeforeMethod(priority = 1)
  public void rowSetup(Object[] params) throws InterruptedException {
    if ((Integer) params[0] == 0) {
      throw new IllegalStateException("row 0 setup fails");
    }
    assertThat(sharedSkipped.await(10, TimeUnit.SECONDS))
        .withFailMessage("the skipped firstTimeOnly setup was not recorded within 10s")
        .isTrue();
  }

  @BeforeMethod(firstTimeOnly = true, priority = 2)
  public void sharedSetup() {}

  @Test(dataProvider = "rows")
  public void t(int row) {}

  /** Releases row 1 once the skipped firstTimeOnly setup has been recorded. */
  public static class Gate implements IConfigurationListener {

    @Override
    public void onConfigurationSkip(ITestResult result) {
      if ("sharedSetup".equals(result.getMethod().getMethodName())) {
        sharedSkipped.countDown();
      }
    }
  }
}
