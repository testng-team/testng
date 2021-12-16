package test.listeners.issue2685;

import org.testng.annotations.Test;

public class InterruptedTestSample {

  @Test(timeOut = 100)
  public void failedTest() {
    throw new AssertionError("Force fail.");
  }

  @Test(timeOut = 100)
  public void timedoutTest() throws InterruptedException {
    Thread.sleep(500);
  }
}
