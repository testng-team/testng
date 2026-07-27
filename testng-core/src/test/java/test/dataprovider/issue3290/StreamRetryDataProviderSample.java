package test.dataprovider.issue3290;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Drives the retry path that re-invokes a {@code cacheDataForTestRetries = false} data provider and
 * consumes only up to the failing index before breaking out (the early-break consumption path in
 * {@code TestInvoker#retryFailed}). {@link #OPEN_COUNT} counts every stream the provider hands out
 * and {@link #CLOSE_COUNT} counts every stream that was closed; the driving test asserts they
 * match, which proves the partially-consumed stream is still closed.
 */
public class StreamRetryDataProviderSample {

  public static final AtomicInteger OPEN_COUNT = new AtomicInteger(0);
  public static final AtomicInteger CLOSE_COUNT = new AtomicInteger(0);

  private final AtomicInteger invocationCount = new AtomicInteger(0);

  @DataProvider(name = "dp", cacheDataForTestRetries = false)
  public Stream<Object[]> getData() {
    OPEN_COUNT.incrementAndGet();
    return Stream.of(new Object[] {"a"}, new Object[] {"b"}).onClose(CLOSE_COUNT::incrementAndGet);
  }

  @Test(dataProvider = "dp", retryAnalyzer = OneRetry.class)
  public void testMethod(String value) {
    // Fail the first invocation so the retry path (which re-invokes the data provider) is
    // exercised.
    if (invocationCount.getAndIncrement() < 1) {
      throw new RuntimeException("Deliberate failure to force a retry for " + value);
    }
  }

  public static class OneRetry implements IRetryAnalyzer {
    private int count = 0;

    @Override
    public boolean retry(ITestResult result) {
      return count++ < 1;
    }
  }
}
