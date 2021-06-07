package test.retryAnalyzer.dataprovider.issue2163;

import java.util.List;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.collections.Lists;

public class RetryAnalyzer implements IRetryAnalyzer {

  private static final int DEFAULT_MAX_RETRY_COUNT = 3;
  private int retryCount = 1;
  public static List<String> logs = Lists.newArrayList();

  @Override
  public boolean retry(ITestResult result) {
    logs.add("Executing " + result.getMethod().getMethodName());
    return retryCount++ < DEFAULT_MAX_RETRY_COUNT;
  }
}
