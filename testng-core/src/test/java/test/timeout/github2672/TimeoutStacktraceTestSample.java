package test.timeout.github2672;

import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadTimeoutException;

class TimeoutStacktraceTestSample {
  @Test(
      description = "testTimeoutStacktrace",
      expectedExceptions = ThreadTimeoutException.class,
      timeOut = 100)
  public void testTimeoutStacktrace() throws InterruptedException {
    Thread.sleep(1000);
  }
}
