package test.factory.issue326;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class LocalTrackingListener implements IInvokedMethodListener {

  private final ConcurrentMap<String, List<Statistics>> results = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Long> threadIds = new ConcurrentHashMap<>();

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    String key = testResult.getInstance().toString();
    results.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(new Statistics(testResult));
    Long threadId = Long.parseLong(testResult.getAttribute(SampleTestClass.THREAD_ID).toString());
    threadIds.putIfAbsent(key, threadId);
  }

  public Map<String, List<Statistics>> getResults() {
    return results;
  }

  public Map<String, Long> getThreadIds() {
    return threadIds;
  }

  public static class Statistics {
    String methodName;
    long startTimeInMs;
    long endTimeInMs;
    long threadId;

    public Statistics(ITestResult testResult) {
      this(
          testResult.getMethod().getMethodName(),
          testResult.getStartMillis(),
          testResult.getEndMillis(),
          Long.parseLong(testResult.getAttribute(SampleTestClass.THREAD_ID).toString()));
    }

    public Statistics(String methodName, long startTimeInMs, long endTimeInMs, long threadId) {
      this.methodName = methodName;
      this.startTimeInMs = startTimeInMs;
      this.endTimeInMs = endTimeInMs;
      this.threadId = threadId;
    }
  }
}
