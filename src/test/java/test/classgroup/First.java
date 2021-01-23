package test.classgroup;

import org.testng.annotations.Test;

@Test(groups = {"first"})
public class First {

  private static boolean m_first1 = false;
  private static boolean m_first2 = false;

  static boolean allRun() {
    return m_first1 && m_first2;
  }

  @Test
  public void first1() {
    m_first1 = true;
  }

  @Test
  public void first2() {
    m_first2 = true;
  }
}
