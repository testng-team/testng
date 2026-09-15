package org.testng.concurrency;

import org.testng.annotations.Test;
import org.testng.concurrency.samples.ThreadPoolSizeBase;

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
