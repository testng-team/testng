package test.listeners.cliwiring;

import java.util.ArrayList;
import java.util.List;

/** Records the order in which the sample listeners below were invoked. */
public final class WiringLog {

  private static final List<String> ENTRIES = new ArrayList<>();

  private WiringLog() {}

  public static void record(String entry) {
    ENTRIES.add(entry);
  }

  public static List<String> entries() {
    return new ArrayList<>(ENTRIES);
  }

  public static void clear() {
    ENTRIES.clear();
  }
}
