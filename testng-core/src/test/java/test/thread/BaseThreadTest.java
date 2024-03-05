package test.thread;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.Assert;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.AutoCloseableLock;
import test.SimpleBaseTest;

public class BaseThreadTest extends SimpleBaseTest {
  private static Set<Long> m_threadIds;
  private static Map<String, Long> m_suitesMap;
  private static List<String> m_strings;

  private static final AutoCloseableLock stringsLock = new AutoCloseableLock();
  private static final AutoCloseableLock threadIdsLock = new AutoCloseableLock();
  private static final AutoCloseableLock suiteMapLock = new AutoCloseableLock();

  static void initThreadLog() {
    m_threadIds = Sets.newHashSet();
    m_suitesMap = Maps.newHashMap();
    m_strings = Lists.newArrayList();
  }

  protected void logString(String s) {
    try (AutoCloseableLock ignore = stringsLock.lock()) {
      log("BaseThreadTest", "Logging string:" + s);
      m_strings.add(s);
    }
  }

  public static List<String> getStrings() {
    return m_strings;
  }

  protected void logCurrentThread() {
    logThread(Thread.currentThread().getId());
  }

  protected void logThread(long threadId) {
    try (AutoCloseableLock ignore = threadIdsLock.lock()) {
      log("BaseThreadTest", "Logging thread:" + threadId);
      m_threadIds.add(threadId);
    }
  }

  protected void logSuite(String suiteName, long time) {
    try (AutoCloseableLock ignore = suiteMapLock.lock()) {
      m_suitesMap.put(suiteName, time);
    }
  }

  static int getThreadCount() {
    try (AutoCloseableLock ignore = threadIdsLock.lock()) {
      return m_threadIds.size();
    }
  }

  static Map<String, Long> getSuitesMap() {
    return m_suitesMap;
  }

  protected void log(String cls, String s) {
    if (false) {
      System.out.println(
          "["
              + cls
              + "] thread:"
              + Thread.currentThread().getId()
              + " hash:"
              + hashCode()
              + " "
              + s);
    }
  }

  protected void verifyThreads(int expected) {
    Assert.assertEquals(
        getThreadCount(),
        expected,
        "Ran on " + getThreadCount() + " threads instead of " + expected);
  }
}
