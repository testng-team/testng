package test.thread;

import org.testng.annotations.Test;

public class ThreadPoolSizeTest extends ThreadPoolSizeBase {
  @Test(invocationCount = 5, threadPoolSize = 3)
  public void f1() {
    logThread();
  }

  @Test(dependsOnMethods = {"f1"})
  public void verify() {
    verifyThreads(3);
  }

}
