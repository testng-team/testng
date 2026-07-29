package test.listeners.cliwiring;

import java.util.List;
import org.testng.collections.Lists;

/** Records the order in which the sample listeners below were invoked. */
public final class WiringLog {

  private static final List<String> ENTRIES = Lists.newArrayList();

  private WiringLog() {}

  public static void record(String entry) {
    ENTRIES.add(entry);
  }

  public static List<String> entries() {
    return Lists.newArrayList(ENTRIES);
  }

  public static void clear() {
    ENTRIES.clear();
  }
}
