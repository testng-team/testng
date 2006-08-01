package test.multiple;

public class Test1 {
  private static int m_count = 0;

  /**
   * @testng.configuration beforeTest = "true
   */
  public void init() {
    m_count = 0;
  }

  /**
   * @testng.test
   */
  public void f1() {
//    ppp("INVOKING f1 " + m_count);
    assert m_count < 1 : "FAILING";
    m_count++;
  }
  
  private static void ppp(String s) {
    System.out.println("[Test1] " + s);
  }
  
}
