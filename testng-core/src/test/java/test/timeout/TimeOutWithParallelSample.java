package test.timeout;

import org.testng.annotations.Test;

public class TimeOutWithParallelSample {

  /**
   * Sleeps three times its time-out. A method that overruns is reported as failed whether or not
   * the time-out was enforced, so only the invocation's duration tells the two apart -- and with
   * the 1500 ms this used to sleep, the pre-existing GITHUB-2009 bound of 2000 ms in {@link
   * TimeOutTest#testTimeOutWhenParallelIsMethods()} passed even against a build that enforced
   * nothing. Three times the time-out is what makes that bound, and the one {@link
   * TimeOutIntegrationTest#testTimeOutWhenParallelIsTest()} adds, bite.
   */
  @Test(timeOut = 1_000)
  public void myTestMethod() throws InterruptedException {
    Thread.sleep(3_000);
  }
}
