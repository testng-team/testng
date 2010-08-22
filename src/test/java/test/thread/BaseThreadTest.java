package test.thread;

import org.testng.internal.annotations.Sets;

import test.SimpleBaseTest;

import java.util.Set;

public class BaseThreadTest extends SimpleBaseTest {
  static private Set<Long> m_threadIds;

  static void initThreadLog() {
    m_threadIds = Sets.newHashSet();
  }

  protected void logThread(long threadId) {
    synchronized(m_threadIds) {
      log("BaseThreadTest", "Logging thread:" + threadId);
      m_threadIds.add(threadId);
    }
  }

  static int getThreadCount() {
    synchronized(m_threadIds) {
      return m_threadIds.size();
    }
  }

  protected void log(String cls, String s) {
    if (false) {
      System.out.println("[" + cls + "] thread:" + Thread.currentThread().getId()
          + " hash:" + hashCode() + " " + s);
    }
  }

}
