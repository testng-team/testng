package test.listeners.issue2752;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(ListenerSample.class)
public class TestClassSample {
  private static final Map<String, Long> timeLogs = new ConcurrentHashMap<>();

  public TestClassSample() {
    timeLogs.clear();
  }

  public static Map<String, Long> getTimeLogs() {
    return timeLogs;
  }

  @BeforeClass
  public void beforeClass() throws InterruptedException {
    TimeUnit.MILLISECONDS.sleep(1);
    timeLogs.put("beforeClass", System.currentTimeMillis());
  }

  @Test
  public void testMethod() throws InterruptedException {
    TimeUnit.MILLISECONDS.sleep(1);
    timeLogs.put("testMethod", System.currentTimeMillis());
  }

  @AfterClass
  public void afterClass() throws InterruptedException {
    TimeUnit.MILLISECONDS.sleep(1);
    timeLogs.put("afterClass", System.currentTimeMillis());
  }
}
