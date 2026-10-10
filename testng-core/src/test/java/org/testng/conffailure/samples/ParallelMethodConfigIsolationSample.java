package org.testng.conffailure.samples;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.testng.IConfigurationListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Two parallel data-provider rows on one instance. Row 0's setup fails. Row 1 must still run.
 *
 * <p>The gate holds row 0 after the failure is recorded and before the shared invocation count
 * moves, so row 1's failure check sees that record if the two rows share a token.
 */
public class ParallelMethodConfigIsolationSample {

  public static CountDownLatch recorded;
  public static CountDownLatch siblingDecided;

  public static void reset() {
    recorded = new CountDownLatch(1);
    siblingDecided = new CountDownLatch(1);
  }

  @DataProvider(name = "rows", parallel = true)
  public Object[][] rows() {
    return new Object[][] {{0}, {1}};
  }

  @BeforeMethod
  public void setup(Object[] params) throws InterruptedException {
    if ((Integer) params[0] == 0) {
      throw new IllegalStateException("setup fails for row 0");
    }
    assertThat(recorded.await(10, TimeUnit.SECONDS))
        .withFailMessage("row 0 did not record its setup failure within 10s")
        .isTrue();
  }

  @Test(dataProvider = "rows")
  public void t(int row) {}

  /** Releases the gate from the hooks that run around the shared invocation count. */
  public static class Gate implements ITestListener, IConfigurationListener {

    @Override
    public void onTestStart(ITestResult result) {
      if (!isRow(result, 0)) {
        siblingDecided.countDown();
        return;
      }
      recorded.countDown();
      await(siblingDecided);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
      if (isRow(result, 1)) {
        siblingDecided.countDown();
      }
    }

    @Override
    public void onConfigurationSkip(ITestResult result) {
      if ("setup".equals(result.getMethod().getMethodName())) {
        siblingDecided.countDown();
      }
    }

    private static boolean isRow(ITestResult result, int row) {
      Object[] parameters = result.getParameters();
      return parameters.length == 1 && Integer.valueOf(row).equals(parameters[0]);
    }

    private static void await(CountDownLatch latch) {
      try {
        assertThat(latch.await(10, TimeUnit.SECONDS))
            .withFailMessage("the sibling row was not decided within 10s")
            .isTrue();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(e);
      }
    }
  }
}
