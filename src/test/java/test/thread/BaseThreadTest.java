package test.thread;

import org.testng.collections.Maps;
import org.testng.internal.annotations.Sets;

import test.SimpleBaseTest;

import java.util.Map;
import java.util.Set;

public class BaseThreadTest extends SimpleBaseTest {
  static private Set<Long> m_threadIds;
  static private Map<String, Long> m_suitesMap;

  static void initThreadLog() {
    m_threadIds = Sets.newHashSet();
    m_suitesMap = Maps.newHashMap();
  }

  protected void logThread(long threadId) {
    synchronized(m_threadIds) {
      log("BaseThreadTest", "Logging thread:" + threadId);
      m_threadIds.add(threadId);
    }
  }

  protected void logSuite(String suiteName, long time) {
    synchronized(m_suitesMap) {
      m_suitesMap.put(suiteName, time);
    }
  }

  static int getThreadCount() {
    synchronized(m_threadIds) {
      return m_threadIds.size();
    }
  }

  static Map<String, Long> getSuitesMap() {
    return m_suitesMap;
  }

  protected void log(String cls, String s) {
    if (false) {
      System.out.println("[" + cls + "] thread:" + Thread.currentThread().getId()
          + " hash:" + hashCode() + " " + s);
    }
  }

}
