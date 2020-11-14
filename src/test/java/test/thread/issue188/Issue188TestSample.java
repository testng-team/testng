package test.thread.issue188;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Issue188TestSample {
  public static final Map<Long, Set<String>> timestamps = new ConcurrentHashMap<>();

  @BeforeMethod
  public void logTime(ITestResult itr) {
    String txt = Reporter.getCurrentTestResult().getTestContext().getName() + "_" + itr.getMethod().getQualifiedName();
    timestamps.computeIfAbsent(System.currentTimeMillis(), k-> ConcurrentHashMap.newKeySet()).add(txt);
  }

  @Test
  public void sampleTest() {
  }

  @Test
  public void anotherSampleTest() {
  }

}