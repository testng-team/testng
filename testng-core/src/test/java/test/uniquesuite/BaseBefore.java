package test.uniquesuite;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class BaseBefore {
  public static int m_beforeCount;
  public static int m_afterCount;

  @BeforeSuite
  public void incrementBefore() {
    m_beforeCount++;
  }

  @AfterSuite
  public void incrementAfter() {
    m_afterCount++;
  }
}
