package test.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Helper {
  private static final Map<String, Map<Long, Long>> m_maps = new ConcurrentHashMap<>();

  public static Map<Long, Long> getMap(String className) {
    return m_maps.computeIfAbsent(className, cn -> new ConcurrentHashMap<>());
  }

  public static void reset() {
    m_maps.clear();
  }
}
