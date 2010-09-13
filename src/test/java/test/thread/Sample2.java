package test.thread;

import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Sample2 extends BaseThreadTest {

  @BeforeMethod
  public void before(ITestContext ctx) {
    logSuite(ctx.getSuite().getName(), System.currentTimeMillis());
  }

  @Test
  public void s1() {
    logThread(Thread.currentThread().getId());
  }
}
