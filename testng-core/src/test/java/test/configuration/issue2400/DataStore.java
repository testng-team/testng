package test.configuration.issue2400;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public enum DataStore {
  INSTANCE;

  private Map<String, AtomicInteger> tracker = new HashMap<>();

  public void increment(String key) {
    tracker.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
  }

  public Map<String, AtomicInteger> getTracker() {
    return tracker;
  }
}
