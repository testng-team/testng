package test.uniquesuite;

import org.testng.annotations.Configuration;

public class BaseBefore {
  public static int m_beforeCount = 0;
  public static int m_afterCount = 0;

  @Configuration(beforeSuite = true)
  public void incrementBefore() {
    m_beforeCount++;
  }
  
  @Configuration(afterSuite = true)
  public void incrementAfter() {
    m_afterCount++;
  }
  
  private static void ppp(String s) {
    System.out.println("[Base] " + s);
  }
}
