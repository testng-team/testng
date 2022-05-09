package test.listeners.issue2752;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.IClassListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ListenerSample implements IClassListener, ITestListener {

  private static final Map<String, List<String>> logs = new ConcurrentHashMap<>();

  public ListenerSample() {
    logs.clear();
  }

  public static Map<String, List<String>> getLogs() {
    return logs;
  }

  @Override
  public void onBeforeClass(ITestClass testClass) {
    String key = testClass.getXmlTest().getName();
    logs.computeIfAbsent(key, k -> new ArrayList<>()).add("onBeforeClass");
  }

  @Override
  public void onAfterClass(ITestClass testClass) {
    String key = testClass.getXmlTest().getName();
    logs.computeIfAbsent(key, k -> new ArrayList<>()).add("onAfterClass");
  }

  @Override
  public void onTestStart(ITestResult result) {
    String key = result.getTestContext().getName();
    logs.computeIfAbsent(key, k -> new ArrayList<>()).add("onTestStart");
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    String key = result.getTestContext().getName();
    logs.computeIfAbsent(key, k -> new ArrayList<>()).add("onTestSuccess");
  }

  @Override
  public void onFinish(ITestContext context) {
    String key = context.getName();
    logs.computeIfAbsent(key, k -> new ArrayList<>()).add("onFinish");
  }

  @Override
  public void onStart(ITestContext context) {
    String key = context.getName();
    logs.computeIfAbsent(key, k -> new ArrayList<>()).add("onStart");
  }
}
