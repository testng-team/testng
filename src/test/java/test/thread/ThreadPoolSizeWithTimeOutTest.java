package test.thread;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import junit.framework.Assert;

public class ThreadPoolSizeWithTimeOutTest extends ThreadPoolSizeBase {

  @Test(invocationCount = 5, threadPoolSize = 3, timeOut = 1000)
  public void f1() {
    logThread();
  }

  @Test(dependsOnMethods = {"f1"})
  public void verify() {
    verifyThreads(3);
  }

  @Test
  public void threadPoolAndTimeOutShouldFail() {
    TestNG tng = create(ThreadPoolSizeSampleTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    Assert.assertEquals(0, tla.getPassedTests().size());
    Assert.assertEquals(1, tla.getFailedTests().size());
  }
}
