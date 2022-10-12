package test.listeners.issue2752;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.testng.IClassListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ListenerSample implements IClassListener, ITestListener {

  private static final Map<String, List<String>> logs = new ConcurrentHashMap<>();
  private static final Map<String, Long> timeLogs = new ConcurrentHashMap<>();

  public ListenerSample() {
    logs.clear();
  }

  public static Map<String, List<String>> getLogs() {
    return logs;
  }

  public static Map<String, Long> getTimeLogs() {
    return timeLogs;
  }

  @Override
  public void onBeforeClass(ITestClass testClass) {
    try {
      TimeUnit.MILLISECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    timeLogs.put("onBeforeClass", System.currentTimeMillis());
    String key = testClass.getXmlTest().getName();
    logs.computeIfAbsent(key, k -> new ArrayList<>()).add("onBeforeClass");
  }

  @Override
  public void onAfterClass(ITestClass testClass) {
    try {
      TimeUnit.MILLISECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    timeLogs.put("onAfterClass", System.currentTimeMillis());
    String key = testClass.getXmlTest().getName();
    logs.computeIfAbsent(key, k -> new ArrayList<>()).add("onAfterClass");
  }

  @Override
  public void onTestStart(ITestResult result) {
    try {
      TimeUnit.MILLISECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    timeLogs.put("onTestStart", System.currentTimeMillis());
    String key = result.getTestContext().getName();
    logs.computeIfAbsent(key, k -> new ArrayList<>()).add("onTestStart");
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    try {
      TimeUnit.MILLISECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    timeLogs.put("onTestSuccess", System.currentTimeMillis());
    String key = result.getTestContext().getName();
    logs.computeIfAbsent(key, k -> new ArrayList<>()).add("onTestSuccess");
  }

  @Override
  public void onFinish(ITestContext context) {
    try {
      TimeUnit.MILLISECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    timeLogs.put("onFinish", System.currentTimeMillis());
    String key = context.getName();
    logs.computeIfAbsent(key, k -> new ArrayList<>()).add("onFinish");
  }

  @Override
  public void onStart(ITestContext context) {
    try {
      TimeUnit.MILLISECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    timeLogs.put("onStart", System.currentTimeMillis());
    String key = context.getName();
    logs.computeIfAbsent(key, k -> new ArrayList<>()).add("onStart");
  }
}
