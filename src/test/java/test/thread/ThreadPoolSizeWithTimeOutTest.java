package test.thread;

import org.testng.annotations.Test;

public class ThreadPoolSizeWithTimeOutTest extends ThreadPoolSizeBase {

  @Test(invocationCount = 5, threadPoolSize = 3, timeOut = 1000)
  public void f1() {
    logThread();
  }

  @Test(dependsOnMethods = {"f1"})
  public void verify() {
    verifyThreads(3);
  }

}
