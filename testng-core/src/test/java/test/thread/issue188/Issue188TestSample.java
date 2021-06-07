package test.thread.issue188;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Issue188TestSample {
  public static final Map<Long, Set<String>> timestamps = new ConcurrentHashMap<>();
  private static final Random random = new Random();

  @BeforeMethod
  public void logTime(ITestResult itr) {
    String txt =
        Reporter.getCurrentTestResult().getTestContext().getName()
            + "_"
            + itr.getMethod().getQualifiedName();
    timestamps
        .computeIfAbsent(System.currentTimeMillis(), k -> ConcurrentHashMap.newKeySet())
        .add(txt);
  }

  @Test
  public void sampleTest() {
    sleepSilently();
  }

  @Test
  public void anotherSampleTest() {
    sleepSilently();
  }

  private void sleepSilently() {
    try {
      TimeUnit.MILLISECONDS.sleep(500 * random.nextInt(10));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
