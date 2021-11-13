package test.timeout.github2440;

import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadTimeoutException;

public class TimeoutTest {
  @Test(description = "First", timeOut = 1000)
  public void test1() throws InterruptedException {
    Thread.sleep(500);
  }

  @Test(description = "Second")
  public void test2() throws InterruptedException {
    Thread.sleep(1000);
  }

  @Test(description = "Third", expectedExceptions = ThreadTimeoutException.class, timeOut = 100)
  public void test3() throws InterruptedException {
    Thread.sleep(1000);
  }
}
