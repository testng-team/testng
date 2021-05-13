package test.thread;

import org.testng.annotations.BeforeClass;

public class ThreadPoolSizeBase extends BaseThreadTest {
  @BeforeClass
  public void setUp() {
    log(getClass().getName(), "Init log ids");
    initThreadLog();
  }

  protected void logThread() {
    long n = Thread.currentThread().getId();
    log(getClass().getName(), "");
    logThread(n);
  }

}
