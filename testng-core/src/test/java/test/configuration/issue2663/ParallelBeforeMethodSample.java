package test.configuration.issue2663;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Under parallel="methods" the whole before/test/after chain of one invocation runs on a single
 * thread, so the recording is keyed by thread: the invocations interleave between threads but the
 * priority order has to hold inside each of them.
 */
public class ParallelBeforeMethodSample {

  private static final Map<String, List<String>> RECORDS = new ConcurrentHashMap<>();

  public static void reset() {
    RECORDS.clear();
  }

  /** @return the recorded configuration method names, grouped by the thread that ran them. */
  public static Map<String, List<String>> recordsByThread() {
    return RECORDS;
  }

  private static void record(String methodName) {
    RECORDS
        .computeIfAbsent(
            Thread.currentThread().getName(),
            thread -> Collections.synchronizedList(new ArrayList<>()))
        .add(methodName);
  }

  @BeforeMethod(priority = 2)
  public void alphaBefore() {
    record("alphaBefore");
  }

  @BeforeMethod(priority = 1)
  public void bravoBefore() {
    record("bravoBefore");
  }

  @Test
  public void testOne() {}

  @Test
  public void testTwo() {}

  @Test
  public void testThree() {}

  @Test
  public void testFour() {}
}
