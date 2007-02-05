package test.factory;

import org.testng.Assert;

public class OddTest {
  private int m_n;
  
  public OddTest(int n) {
    m_n = n;
  }
  
  /**
   * @testng.test
   */
  public void verify() {
    Assert.assertTrue(m_n % 2 == 1);
  }
  
  private static void ppp(String s) {
    System.out.println("[OddTest] " + s);
  }
}
