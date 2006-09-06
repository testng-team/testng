package test.thread;

import java.util.HashMap;
import java.util.Map;


/**
 * @testng.test sequential = "true"
 * 
 * Created on Aug 14, 2006
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class SequentialSampleTest {

  static Map m_threads  = null;
  
  /**
   * @testng.before-class
   */
  public void setUp() {
    m_threads = new HashMap();
  }

  /**
   * @testng.test
   */
  public void f1() {
    Long id = new Long(Thread.currentThread().hashCode());
    ppp("ID:" + id);
    m_threads.put(id, id);
  }
  
  /**
   * @testng.test
   */
  public void f2() {
    Long id = new Long(Thread.currentThread().hashCode());
    ppp("ID:" + id);
    m_threads.put(id, id);
  }
  
  /**
   * @testng.test
   */
  public void f3() {
    Long id = new Long(Thread.currentThread().hashCode());
    ppp("ID:" + id);
    m_threads.put(id, id);
  }
  
  private static void ppp(String s) {
    if (false) {
      System.out.println("[SequentialSampleTest] " + s);
    }
  }

}
