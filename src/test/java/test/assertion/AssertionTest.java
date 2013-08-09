package test.assertion;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.LoggingAssert;

public class AssertionTest {
  private LoggingAssert m_assert;

  @BeforeMethod
  public void bm() {
    m_assert = new LoggingAssert();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void test1() {
    m_assert.assertTrue(false, "new TestNG Assertion Failed");
  }
}
