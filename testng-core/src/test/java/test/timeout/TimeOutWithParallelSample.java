package test.timeout;

import org.testng.annotations.Test;

public class TimeOutWithParallelSample {

  /**
   * Sleeps three times its time-out so that {@link TimeOutIntegrationTest} and {@link TimeOutTest}
   * can tell an enforced time-out from an unenforced one: both bound the invocation at twice the
   * time-out, and a method left to run to completion is reported as failed just the same, so
   * without that gap the duration would land on the bound rather than either side of it.
   */
  @Test(timeOut = 1_000)
  public void myTestMethod() throws InterruptedException {
    Thread.sleep(3_000);
  }
}
