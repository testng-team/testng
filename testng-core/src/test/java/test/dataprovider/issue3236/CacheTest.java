package test.dataprovider.issue3236;

import static org.testng.Assert.assertEquals;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CacheTest {

  public static final AtomicInteger invocationCount = new AtomicInteger(0);
  private String currentUuid;

  @Test(dataProvider = "dp", retryAnalyzer = MyRetry.class)
  public void testMethod(String uuid) {
    assertEquals(currentUuid, uuid);
    if (invocationCount.get() != 2) {
      throw new RuntimeException("Failed for " + uuid);
    }
  }

  @DataProvider(name = "dp", cacheDataForTestRetries = false)
  public Object[][] getData() {
    invocationCount.incrementAndGet();
    currentUuid = UUID.randomUUID().toString();
    return new Object[][] {{currentUuid}};
  }

  public static class MyRetry implements IRetryAnalyzer {

    private final AtomicInteger counter = new AtomicInteger(1);

    @Override
    public boolean retry(ITestResult result) {
      return counter.getAndIncrement() != 2;
    }
  }
}
