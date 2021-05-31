package test.thread.parallelization.issue1773;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LogGatheringListener {

    private static Set<Long> log = ConcurrentHashMap.newKeySet();

    public static Set<Long> getLog() {
        return log;
    }

    public static void addLog(long threadId) {
        log.add(threadId);
    }

    public static void reset() {
        log = ConcurrentHashMap.newKeySet();
    }
}
