package org.testng.reporters.snapshot;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Run as two {@code <test>} contexts of one parallel suite, each naming its own value after the
 * context it belongs to. No invocation mutates the value it was given until both contexts are
 * running, so one of them finishes reporting while the other still needs its snapshots.
 *
 * <p>Its latch has to span two instances of this class, so unlike its siblings here it cannot be
 * per-instance state, and a second run in the same JVM finds it spent -- passing without the
 * contexts ever having overlapped. One reporter at a time, then: give a second one its own sample
 * rather than adding a run to this one.
 */
public class OverlappingContextsSample {

  private static final int CONTEXTS = 2;
  private static final CountDownLatch BOTH_STARTED = new CountDownLatch(CONTEXTS);

  @DataProvider(name = "value")
  public static Object[][] value(ITestContext context) {
    return new Object[][] {{new MutableParameter(context.getName() + "-context")}};
  }

  @Test(dataProvider = "value")
  public void report(MutableParameter parameter) throws InterruptedException {
    BOTH_STARTED.countDown();
    // Bounded, so that contexts which did not overlap leave the samples sequential rather than
    // hung; the assertions still hold.
    BOTH_STARTED.await(30, TimeUnit.SECONDS);
    parameter.set("mutated");
  }
}
