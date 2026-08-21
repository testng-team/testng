package test.invocationcount;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** The parallel-data-provider twin of {@link CancelledSequentialRows}. */
public class CancelledParallelRows {

  @DataProvider(name = "rows", parallel = true)
  public static Object[][] rows() {
    return new Object[][] {{"fails-a"}, {"fails-b"}, {"passes"}};
  }

  @Test(dataProvider = "rows", invocationCount = 2, skipFailedInvocations = true)
  public void cancelled(String row) {
    if (row.startsWith("fails")) {
      throw new IllegalStateException("this row fails on purpose");
    }
  }
}
