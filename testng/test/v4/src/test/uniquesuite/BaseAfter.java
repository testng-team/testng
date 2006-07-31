package test.uniquesuite;

import org.testng.annotations.Configuration;

public class BaseAfter {
  public static int m_afterCount = 0;

  @Configuration(beforeSuite = true)
  public void beforeSuite() {
    m_afterCount = 0;
  }

  @Configuration(afterSuite = true)
  public void incrementAfter() {
    m_afterCount++;
  }
  
  private static void ppp(String s) {
    System.out.println("[BaseAfter] " + s);
  }
}
