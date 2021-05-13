package test.timeout;

import org.testng.annotations.Test;

/**
 * This class tests timeouts
 *
 * @author cbeust
 */
public class TimeOutSampleTest {

  @Test(timeOut = 5_000 /* 5 seconds */)
  public void timeoutShouldPass() {
  }

  @Test(timeOut = 5_000 /* 5 seconds */)
  public void timeoutShouldFailByException() {
    throw new RuntimeException("EXCEPTION SHOULD MAKE THIS METHOD FAIL");
  }

  @Test(timeOut = 1_000 /* 1 second */)
  public void timeoutShouldFailByTimeOut() throws InterruptedException {
      Thread.sleep(10_000 /* 10 seconds */);
  }
}
