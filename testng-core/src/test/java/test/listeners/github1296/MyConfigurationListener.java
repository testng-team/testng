package test.listeners.github1296;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.testng.IConfigurationListener;
import org.testng.ITestResult;

public class MyConfigurationListener implements IConfigurationListener {

  private static final Map<String, AtomicInteger> CALLS = new ConcurrentHashMap<>();

  public static void clearCalls() {
    CALLS.clear();
  }

  public static Map<String, Integer> getCalls() {
    return CALLS.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, each -> each.getValue().get()));
  }

  @Override
  public void onConfigurationSuccess(ITestResult itr) {
    String xmlTestName = itr.getTestContext().getCurrentXmlTest().getName();
    CALLS.computeIfAbsent(xmlTestName, k -> new AtomicInteger()).incrementAndGet();
  }

  @Override
  public void onConfigurationFailure(ITestResult iTestResult) {}

  @Override
  public void onConfigurationSkip(ITestResult itr) {}
}
