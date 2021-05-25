package test.uniquesuite;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class BaseAfter {
  public static int m_afterCount = 0;

  @BeforeSuite
  public void beforeSuite() {
    m_afterCount = 0;
  }

  @AfterSuite
  public void incrementAfter() {
    m_afterCount++;
  }

  private static void ppp(String s) {
    System.out.println("[BaseAfter] " + s);
  }
}
