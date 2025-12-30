package test.dataprovider.issue3237;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SampleCacheTestCase {

  private final AtomicInteger invocationCount = new AtomicInteger(0);
  private String currentUuid;

  @Test(dataProvider = "dp", retryAnalyzer = MyRetry.class)
  public void testMethod(String uuid) {
    // Verify that fresh UUID is generated on each retry (no caching)
    Assert.assertEquals(uuid, currentUuid, "Received stale data from DataProvider!");

    // Fail on first invocation to trigger retry, pass on second
    if (invocationCount.getAndIncrement() < 1) {
      throw new RuntimeException("Failed for " + uuid);
    }
  }

  @DataProvider(name = "dp", cacheDataForTestRetries = false)
  public Object[][] getData() {
    currentUuid = UUID.randomUUID().toString();
    return new Object[][] {{currentUuid}};
  }

  public static class MyRetry implements IRetryAnalyzer {
    private int count = 0;

    @Override
    public boolean retry(ITestResult result) {
      return count++ < 1;
    }
  }
}
