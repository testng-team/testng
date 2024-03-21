package test.dataprovider.issue3081;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.support.SafeRandoms;

public class TestClassWithPrioritiesSample {
  private static final Set<Long> logs = ConcurrentHashMap.newKeySet();

  public static Set<Long> getLogs() {
    return Collections.unmodifiableSet(logs);
  }

  public static void clear() {
    logs.clear();
  }

  @DataProvider(parallel = true)
  public static Object[] parallelDpStrings() {
    return IntStream.rangeClosed(0, 99).mapToObj(it -> "string " + it).toArray(String[]::new);
  }

  @Test(dataProvider = "parallelDpStrings", priority = 1)
  public void testStrings(String ignored) throws InterruptedException {
    print();
    TimeUnit.MILLISECONDS.sleep(SafeRandoms.nextInt(200, 300));
  }

  @Test(priority = 2)
  public void anotherTest() {}

  private static void print() {
    logs.add(Thread.currentThread().getId());
  }
}
