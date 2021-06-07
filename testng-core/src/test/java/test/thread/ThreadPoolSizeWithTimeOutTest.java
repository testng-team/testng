package test.thread;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
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

  @Test
  public void threadPoolAndTimeOutShouldFail() {
    TestNG tng = create(ThreadPoolSizeSampleTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();

    assertThat(tla.getPassedTests()).isEmpty();
    assertThat(tla.getFailedTests()).hasSize(1);
  }
}
