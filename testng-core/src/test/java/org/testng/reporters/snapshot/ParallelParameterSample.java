package org.testng.reporters.snapshot;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Every row gets its own mutable object, and no invocation mutates its own until all of them are in
 * flight. A reporter that mixed up which snapshot belongs to which result would report a value some
 * other invocation ran with, or the mutated one.
 *
 * <p>The latch belongs to the instance, not the class: a spent one lets a second run pass without
 * its invocations ever having overlapped, which is the whole point of the sample.
 */
public class ParallelParameterSample {

  public static final int ROWS = 4;

  private final CountDownLatch allStarted = new CountDownLatch(ROWS);

  @DataProvider(name = "rows", parallel = true)
  public static Object[][] rows() {
    Object[][] rows = new Object[ROWS][1];
    for (int i = 0; i < ROWS; i++) {
      rows[i][0] = new MutableParameter("row-" + i);
    }
    return rows;
  }

  @Test(dataProvider = "rows")
  public void report(MutableParameter parameter) throws InterruptedException {
    allStarted.countDown();
    // A timeout rather than a plain await: should the rows not run concurrently, the sample
    // degrades to a sequential one instead of hanging, and still asserts what it is here for.
    allStarted.await(30, TimeUnit.SECONDS);
    parameter.set("mutated");
  }
}
