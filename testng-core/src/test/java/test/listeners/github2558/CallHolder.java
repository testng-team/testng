package test.listeners.github2558;

import java.util.List;
import org.assertj.core.util.Lists;

public class CallHolder {

  private static final List<String> calls = Lists.newArrayList();

  public static void addCall(String identifier) {
    calls.add(identifier);
  }

  public static void clear() {
    calls.clear();
  }

  public static List<String> getCalls() {
    return calls;
  }
}
