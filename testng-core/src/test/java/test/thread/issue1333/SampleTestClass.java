package test.thread.issue1333;

import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SampleTestClass {

  @BeforeClass
  public void beforeClass(ITestContext context) {
    record(context);
  }

  @Test
  public void firstTest(ITestContext context) {
    record(context);
  }

  @Test
  public void secondTest(ITestContext context) {
    record(context);
  }

  private static void record(ITestContext context) {
    ThreadIdRecorder.record(context.getName(), Thread.currentThread().getId());
  }
}
