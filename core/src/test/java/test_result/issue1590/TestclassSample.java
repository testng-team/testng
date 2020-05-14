package test_result.issue1590;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestclassSample {
  static int startStatus;
  static int endStatus;
  static long startTimestamp;
  static long endTimestamp;

  @BeforeClass
  public void beforeClass(ITestContext context) throws InterruptedException {
    TimeUnit.SECONDS.sleep(1);
    ITestResult result = context.getSuite().getAllInvokedMethods().get(0).getTestResult();
    startStatus = result.getStatus();
    startTimestamp = result.getEndMillis();
  }

  @Test
  public void testMethod() {
  }

  @AfterClass
  public void afterClass(ITestContext context) {
    ITestResult result = context.getSuite().getAllInvokedMethods().get(0).getTestResult();
    endTimestamp = result.getEndMillis();
    endStatus = ITestResult.SUCCESS;
  }
}
