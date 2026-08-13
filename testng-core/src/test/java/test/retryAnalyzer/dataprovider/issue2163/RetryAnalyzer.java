package test.retryAnalyzer.dataprovider.issue2163;

import java.util.ArrayList;
import java.util.List;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

  private static final int DEFAULT_MAX_RETRY_COUNT = 3;
  private int retryCount = 1;
  public static List<String> logs = new ArrayList<>();

  @Override
  public boolean retry(ITestResult result) {
    logs.add("Executing " + result.getMethod().getMethodName());
    return retryCount++ < DEFAULT_MAX_RETRY_COUNT;
  }
}
