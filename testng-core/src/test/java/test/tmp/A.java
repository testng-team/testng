package test.tmp;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

@Test
public class A {
//  private FlexibleAssert m_assert = new FlexibleAssert();
  private SoftAssert m_assert = new SoftAssert();
//  private LoggingAssert m_assert = new LoggingAssert();

  public void test1() {
    m_assert.assertTrue(true, "test1()");
  }

  public void test2() {
    m_assert.assertTrue(true, "test2()");
  }

//  @AfterClass
//  public void ac() {
//    System.out.println("Tests run in this class:" + m_assert.getMessages());
//  }

  public void multiple() {
    m_assert.assertTrue(true, "Success 1");
    m_assert.assertTrue(true, "Success 2");
    m_assert.assertTrue(false, "Failure 1");
    m_assert.assertTrue(true, "Success 3");
    m_assert.assertTrue(false, "Failure 2");
    m_assert.assertAll();
  }
}