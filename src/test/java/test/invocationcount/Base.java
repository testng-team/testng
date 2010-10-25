package test.invocationcount;

import org.testng.annotations.BeforeClass;

public class Base {
  private static int m_beforeCount;
  private static int m_afterCount;

  @BeforeClass
  public void init() {
//    System.out.println("RESETTING COUNTS");
    m_beforeCount = 0;
    m_afterCount = 0;
  }

  protected void incrementBefore() {
    m_beforeCount++;
//    System.out.println("INC BEFORE:" + m_beforeCount);
  }

  protected void incrementAfter() {
    m_afterCount++;
//    System.out.println("INC AFTER:" + m_beforeCount);
  }

  public static int getBeforeCount() {
    return m_beforeCount;
  }

  public static int getAfterCount() {
    return m_afterCount;
  }
}
