package test.thread.issue1333;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records the thread ids used by the invocations of one {@code <test>} tag, so that the affinity of
 * each tag can be asserted on its own when several of them reference the same sample class -- the
 * shape of the GITHUB-1333 reproducer.
 *
 * <p>The nearest existing recorder is {@code
 * test.thread.parallelization.issue1773.LogGatheringListener}, which is keyed by {@code <test>}
 * name as well. It is not reused here because it carries a second dimension per class that this
 * test has no use for, and because its innermost set is a plain {@link java.util.HashSet} written
 * outside the enclosing map's atomic region: under the bug being reproduced the invocations of one
 * {@code <test>} run on different threads, which is precisely when that set stops being safe to
 * write.
 */
public class ThreadIdRecorder {

  private static final Map<String, Set<Long>> THREAD_IDS = new ConcurrentHashMap<>();

  private ThreadIdRecorder() {}

  public static void record(String testName, long threadId) {
    THREAD_IDS.computeIfAbsent(testName, name -> ConcurrentHashMap.newKeySet()).add(threadId);
  }

  public static Set<Long> getThreadIds(String testName) {
    return THREAD_IDS.getOrDefault(testName, Collections.emptySet());
  }

  public static void reset() {
    THREAD_IDS.clear();
  }
}
