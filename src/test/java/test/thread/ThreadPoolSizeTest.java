package test.thread;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ThreadPoolSizeTest extends BaseThreadTest {
  @BeforeClass
  public void setUp() {
    log(getClass().getName(), "Init log ids");
    initThreadLog();
  }

  @Test(invocationCount = 5, threadPoolSize = 3)
  public void f1() {
    long n = Thread.currentThread().getId();
    log(getClass().getName(), "threadPoolSize:20");
    logThread(n);
  }

  @Test(dependsOnMethods = {"f1"})
  public void verify() {
    verifyThreadCount(3);
  }

}
