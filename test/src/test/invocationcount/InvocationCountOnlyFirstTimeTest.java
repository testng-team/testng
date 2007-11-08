package test.invocationcount;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "first")
public class InvocationCountOnlyFirstTimeTest {
  static int m_count;
  static int m_beforeCount;
  static int m_afterCount;

  @BeforeClass
  public void beforeClass() {
    m_count = 0;
    m_beforeCount = -1;
    m_afterCount = -1;
  }
  @BeforeMethod(firstTimeOnly = true)
  public void beforeMethod() {
    m_beforeCount = m_count;
  }
  
  @Test(invocationCount = 10)
  public void f() {
    m_count++;
  }

  @AfterMethod(lastTimeOnly = true)
  public void afterMethod() {
    m_afterCount = m_count;
  }
}
