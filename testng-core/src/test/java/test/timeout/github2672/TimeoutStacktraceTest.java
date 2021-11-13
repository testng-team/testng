package test.timeout.github2672;

import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadTimeoutException;

import java.util.Arrays;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Listeners(TimeoutStacktraceTest.class)
public class TimeoutStacktraceTest implements ITestListener {

  @Test(description = "testTimeoutStacktrace",
      expectedExceptions = ThreadTimeoutException.class,
      timeOut = 100)
  public void testTimeoutStacktrace() throws InterruptedException {
    Thread.sleep(1000);
  }

  private static void checkResults(ITestResult result) {
    Throwable testError = result.getThrowable();
    assertTrue(testError instanceof ThreadTimeoutException);
    assertTrue(Arrays.stream(testError.getStackTrace())
        .anyMatch(s -> s.getMethodName().equals("testTimeoutStacktrace")));
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    checkResults(result);
  }

  @Override
  public void onTestFailedWithTimeout(ITestResult result) {
    fail("Should not time out");
  }
}
