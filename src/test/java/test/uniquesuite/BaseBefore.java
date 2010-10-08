package test.uniquesuite;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class BaseBefore {
  public static int m_beforeCount = 0;
  public static int m_afterCount = 0;

  @BeforeSuite
  public void incrementBefore() {
    m_beforeCount++;
  }

  @AfterSuite
  public void incrementAfter() {
    m_afterCount++;
  }

  private static void ppp(String s) {
    System.out.println("[Base] " + s);
  }
}
