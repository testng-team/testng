package test.dataprovider.issue3236;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IssueTest {

  private static final AtomicInteger invocationCount = new AtomicInteger(0);
  private static long lastValue = 0;

  @Test(dataProvider = "dp", retryAnalyzer = MyRetry.class)
  public void testMethod(long value) {
    int count = invocationCount.getAndIncrement();
    if (count == 0) {
        lastValue = value;
        throw new RuntimeException("Fail first time to trigger retry");
    } else {
        // Retry
        Assert.assertNotEquals(value, lastValue, "DataProvider should have been re-executed and returned a new value");
    }
  }

  @DataProvider(name = "dp", cacheDataForTestRetries = false)
  public Object[][] getData() {
    return new Object[][] {{ System.nanoTime() }};
  }

  public static class MyRetry implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int maxRetryCount = 1;

    @Override
    public boolean retry(ITestResult result) {
      if (retryCount < maxRetryCount) {
        retryCount++;
        return true;
      }
      return false;
    }
  }
}
