package org.testng.timeout.samples.issue3513;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;

/**
 * The first invocation returns at once. The second ignores interruption and overruns a suite
 * time-out of a few hundred milliseconds.
 */
public class MixedInvocationSample {

  private final AtomicInteger invocations = new AtomicInteger();

  @Test(invocationCount = 2)
  public void mixedInvocations() {
    if (invocations.getAndIncrement() == 0) {
      return;
    }
    IgnoreInterruption.forMillis(2_000);
  }
}
