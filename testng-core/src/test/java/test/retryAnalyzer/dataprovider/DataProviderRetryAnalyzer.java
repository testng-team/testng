package test.retryAnalyzer.dataprovider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class DataProviderRetryAnalyzer implements IRetryAnalyzer {

  private static final int MAX_RETRY_COUNT = 3;

  private final Map<Integer, AtomicInteger> counts = new HashMap<>();

  private AtomicInteger getCount(ITestResult result) {
    int id = Arrays.hashCode(result.getParameters());
    AtomicInteger count = counts.get(id);
    if (count == null) {
      count = new AtomicInteger(MAX_RETRY_COUNT);
      counts.put(id, count);
    }
    return count;
  }

  @Override
  public boolean retry(ITestResult result) {
    int retriesRemaining = getCount(result).getAndDecrement();
    return retriesRemaining > 0;
  }
}
