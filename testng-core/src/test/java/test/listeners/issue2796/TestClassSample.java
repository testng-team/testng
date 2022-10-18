package test.listeners.issue2796;

import java.util.LinkedHashSet;
import java.util.Set;
import org.testng.IClassListener;
import org.testng.ITestClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import test.listeners.issue2796.TestClassSample.LocalLogs;

@Listeners(LocalLogs.class)
public class TestClassSample {

  @BeforeClass
  public void beforeClass() {
    TestLogAppender.addLog("beforeClass");
  }

  @Test
  public void testMethod() {}

  @AfterClass
  public void afterClass() {
    TestLogAppender.addLog("afterClass");
  }

  public static class LocalLogs implements IClassListener {

    @Override
    public void onBeforeClass(ITestClass testClass) {
      TestLogAppender.addLog("onBeforeClass");
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
      TestLogAppender.addLog("onAfterClass");
    }
  }

  public static class TestLogAppender {

    private static final Set<String> logs = new LinkedHashSet<>();

    public static Set<String> getLogs() {
      return logs;
    }

    public static synchronized void addLog(String method) {
      logs.add(method);
    }
  }
}
