package test.retryAnalyzer.issue3231;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public class InvocationCountRetrySample {

  public static class MyRetry implements IRetryAnalyzer {
    private int count = 0;

    @Override
    public boolean retry(ITestResult result) {
      return count++ < 1; // Retry once
    }
  }

  public static final AtomicInteger invocationCount = new AtomicInteger(0);

  @Test(
      invocationCount = 2,
      retryAnalyzer = MyRetry.class)
  public void test() {
    invocationCount.incrementAndGet();
    Assert.fail("fail");
  }
}