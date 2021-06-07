package test.thread.parallelization.issue1773;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LogGatheringListener {

  private static Map<String, Map<Class<?>, Set<Long>>> log = new ConcurrentHashMap<>();

  public static Map<String, Map<Class<?>, Set<Long>>> getLog() {
    return log;
  }

  public static void addLog(String testname, Class<?> cls, long threadId) {
    log.computeIfAbsent(testname, t -> new ConcurrentHashMap<>())
        .computeIfAbsent(cls, c -> new HashSet<>())
        .add(threadId);
  }

  public static void reset() {
    log = new ConcurrentHashMap<>();
  }
}
