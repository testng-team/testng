package test.thread;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;

public class ThreadPoolSizeTest {
  private static Map m_threadIds;
  
  /**
   * @testng.configuration beforeTestClass = "true"
   */
  public void setUp() {
    ppp("INIT THREAD IDS");
    m_threadIds = new HashMap();    
  }

  /**
   * @testng.test invocationCount = "10" threadPoolSize = "3"
   */
  public void f1() {
    Long n = new Long(Thread.currentThread().hashCode());
    ppp("THREAD: " + n + " " +hashCode());
    m_threadIds.put(n,n);
  }

  /**
   * @testng.test dependsOnMethods = "f1"
   */
  public void verify() {
    int expected = 3;
    Assert.assertEquals(m_threadIds.size(), expected,  
        "Should have run on " + expected + " threads but ran on " + m_threadIds.size());
  }
  
  private static void ppp(String s) {
    if (false) {
      System.out.println("[ThreadPoolSizeTest] " + s);
    }
  }
}
