package test.multiple;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public class Test1 {
  private static int m_count = 0;

  @Test
  public void f1() {
//    ppp("INVOKING f1 " + m_count);
    assertTrue(m_count < 1, "FAILING");
    m_count++;
  }

  @AfterTest
  public void cleanUp() {
    m_count = 0;
  }


  private static void ppp(String s) {
    System.out.println("[Test1] " + s);
  }

}
