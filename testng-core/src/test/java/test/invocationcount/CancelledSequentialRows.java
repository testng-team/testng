package test.invocationcount;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Several rows, two of which fail, with the {@code invocationCount}s still to come cancelled.
 *
 * <p>What an invocationCount counts is repetitions of the whole data set, not rows, so cancelling
 * the ones still to come says nothing about the rows of the repetition under way: every row runs,
 * whichever of them failed first.
 *
 * <p>{@link CancelledParallelRows} is the same method against a parallel data provider, where the
 * rows race to cancel.
 */
public class CancelledSequentialRows {

  @DataProvider(name = "rows")
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
