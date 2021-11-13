package test.thread;

import org.testng.annotations.Test;

public class ThreadPoolSampleBugTest {
  private static final long TIMEOUT = 500;

  @Test(invocationCount = 1, threadPoolSize = 5)
  public void shouldPass1() throws InterruptedException {
    Thread.sleep(TIMEOUT);
  }

  @Test(invocationCount = 2, threadPoolSize = 5)
  public void shouldPass2() throws InterruptedException {
    Thread.sleep(TIMEOUT);
  }

  @Test(timeOut = 10, invocationCount = 1, threadPoolSize = 5)
  public void shouldFail1() throws InterruptedException {
    Thread.sleep(TIMEOUT);
  }

  @Test(timeOut = 10, invocationCount = 2, threadPoolSize = 5)
  public void shouldFail2() throws InterruptedException {
    Thread.sleep(TIMEOUT);
  }
}
