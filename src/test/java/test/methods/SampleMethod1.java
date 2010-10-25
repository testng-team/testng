package test.methods;

import org.testng.annotations.Test;

/**
 * This class is used to test invocation of methods
 *
 * @author cbeust
 */
public class SampleMethod1 {
  private static boolean m_ok1 = false;
  private static boolean m_ok2 = false;
  private static boolean m_ok3 = true;
  private static boolean m_ok4 = true;

  public static void reset() {
    m_ok1 = false;
    m_ok2 = false;
    m_ok3 = true;
    m_ok4 = true;
  }

  @Test(groups = { "sample1" })
  public void shouldRun1() {
    m_ok1 = true;
  }

  @Test(groups = { "sample1" })
  public void shouldRun2() {
    m_ok2 = true;
  }

  @Test
  public void shouldNotRun1() {
    m_ok3 = false;
  }

  @Test
  public void shouldNotRun2() {
    m_ok4 = false;
  }

  public static void verify() {
    assert m_ok1 && m_ok2 && m_ok3 && m_ok4 :
      "All booleans should be true: " + m_ok1 + " " + m_ok2
      + " " + m_ok3 + " " + m_ok4;
  }
  static private void ppp(String s) {
    System.out.println("[SampleMethod1] " + s);
  }

}
