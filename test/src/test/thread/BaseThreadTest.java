package test.thread;

import java.util.Set;

import test.SimpleBaseTest;

import com.google.inject.internal.Sets;

public class BaseThreadTest extends SimpleBaseTest {
  static private Set<Long> m_threadIds;

  protected void initThreadLog() {
    m_threadIds = Sets.newHashSet();
  }

  protected void logThread(long threadId) {
    synchronized(m_threadIds) {
      m_threadIds.add(threadId);
    }
  }

  public static int getThreadCount() {
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
