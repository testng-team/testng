package test.factory;

import org.testng.Assert;
import org.testng.annotations.Test;

public class OddTest {
  private int m_n;

  public OddTest(int n) {
    m_n = n;
  }

  @Test
  public void verify() {
    Assert.assertTrue(m_n % 2 != 0);
  }

  private static void ppp(String s) {
    System.out.println("[OddTest] " + s);
  }
}
