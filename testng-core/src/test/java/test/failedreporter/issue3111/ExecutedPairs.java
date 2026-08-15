package test.failedreporter.issue3111;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/** Records which factory instances actually ran their test method, across a run and its re-run. */
public final class ExecutedPairs {

  private static final Set<Integer> INSTANCES = new ConcurrentSkipListSet<>();

  /** Factory instance index paired with the data provider row, as "instance/row". */
  private static final Set<String> PAIRS = new ConcurrentSkipListSet<>();

  private ExecutedPairs() {}

  static void record(int instance) {
    INSTANCES.add(instance);
  }

  static void record(int instance, int row) {
    PAIRS.add(instance + "/" + row);
  }

  static void clear() {
    INSTANCES.clear();
    PAIRS.clear();
  }

  static List<Integer> instances() {
    return INSTANCES.stream().sorted().collect(Collectors.toList());
  }

  static List<String> pairs() {
    return PAIRS.stream().sorted().collect(Collectors.toList());
  }
}
