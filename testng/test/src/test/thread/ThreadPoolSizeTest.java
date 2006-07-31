package test.thread;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ThreadPoolSizeTest {
  private static Map<Long, Long> m_threadIds;
  
  @BeforeClass
  public void setUp() {
    ppp("INIT THREAD IDS");
    m_threadIds = new HashMap<Long, Long>();    
  }

  @Test(invocationCount = 10, threadPoolSize = 3)
  public void f1() {
    long n = Thread.currentThread().getId();
    ppp("THREAD: " + n + " " +hashCode());
    m_threadIds.put(n,n);
  }
  
  @Test(dependsOnMethods = {"f1"})
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
