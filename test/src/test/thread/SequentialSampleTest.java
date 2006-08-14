package test.thread;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(sequential = true)
public class SequentialSampleTest {

  static Map<Long, Long> m_threads = null;
  
  @BeforeClass
  public void setUp() {
    m_threads = new HashMap<Long, Long>();
  }

  @Test
  public void f1() {
    long id =Thread.currentThread().getId();
    ppp("ID:" + id);
    m_threads.put(id, id);
  }
  
  @Test
  public void f2() {
    long id =Thread.currentThread().getId();
    ppp("ID:" + id);
    m_threads.put(id, id);
  }
  
  @Test
  public void f3() {
    long id =Thread.currentThread().getId();
    ppp("ID:" + id);
    m_threads.put(id, id);
  }
  
  private static void ppp(String s) {
    if (false) {
      System.out.println("[SequentialSampleTest] " + s);
    }
  }

}
