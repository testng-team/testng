package test.retryAnalyzer.issue1946;

import java.util.Arrays;
import java.util.List;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.collections.Lists;

public class RetryAnalyzer implements IRetryAnalyzer {

  public static List<String> logs = Lists.newArrayList();

  private int retryCount = 0;
  private static final int MAX_RETRY_COUNT = 1;

  public boolean retry(ITestResult result) {
    String prefix = "Attempt #" + retryCount;
    if (retryCount < MAX_RETRY_COUNT) {
      if (result.getParameters().length > 0) {
        logs.add(prefix + ". Retry :true  " + prettyMsg(result));
      }
      retryCount++;
      return true;
    }
    logs.add(prefix + ". Retry :false " + prettyMsg(result));
    return false;
  }

  private static String prettyMsg(ITestResult result) {
    return "Test method : "
        + result.getTestClass().getRealClass().getName()
        + "."
        + result.getMethod().getMethodName()
        + "()"
        + ", Parameters : "
        + Arrays.toString(result.getParameters());
  }
}
