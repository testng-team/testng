package test.dataprovider.issue3041;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SampleTestCase {

  public static final AtomicInteger invocationCount = new AtomicInteger(0);
  private static final Random random = new Random();

  @Test(dataProvider = "dp", retryAnalyzer = MyRetry.class)
  public void testMethod(int i) {
    if (invocationCount.get() != 2) {
      throw new RuntimeException("Failed for " + i);
    }
  }

  @DataProvider(name = "dp", cacheDataForTestRetries = false)
  public Object[][] getData() {
    invocationCount.incrementAndGet();
    return new Object[][] {{next()}, {next()}};
  }

  private static int next() {
    return random.nextInt();
  }

  public static class MyRetry implements IRetryAnalyzer {

    private final AtomicInteger counter = new AtomicInteger(1);

    @Override
    public boolean retry(ITestResult result) {
      return counter.getAndIncrement() != 2;
    }
  }
}
