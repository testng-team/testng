package test.factory.issue326;

import java.util.List;
import java.util.Map;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

public class LocalTrackingListener implements IInvokedMethodListener {

  private final Map<String, List<Statistics>> results = Maps.newConcurrentMap();
  private final Map<String, Long> threadIds = Maps.newConcurrentMap();

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    String key = testResult.getInstance().toString();
    results.computeIfAbsent(key, k -> Lists.newArrayList()).add(new Statistics(testResult));
    if (!threadIds.containsKey(key)) {
      Long threadId = Long.parseLong(testResult.getAttribute(SampleTestClass.THREAD_ID).toString());
      threadIds.put(key, threadId);
    }
  }

  public Map<String, List<Statistics>> getResults() {
    return results;
  }

  public Map<String, Long> getThreadIds() {
    return threadIds;
  }

  static class Statistics {
    String methodName;
    long startTimeInMs;

    public Statistics(ITestResult testResult) {
      this(testResult.getMethod().getMethodName(), testResult.getStartMillis());
    }

    public Statistics(String methodName, long startTimeInMs) {
      this.methodName = methodName;
      this.startTimeInMs = startTimeInMs;
    }
  }
}
