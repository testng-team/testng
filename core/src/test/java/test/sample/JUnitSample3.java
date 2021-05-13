package test.sample;

import org.testng.Assert;

import junit.framework.TestCase;

/**
 * This class verifies that a new instance is used every time
 *
 * @author cbeust
 */
public class JUnitSample3 extends TestCase {
  private int m_count = 0;

  public void test1() {
    Assert.assertEquals( m_count, 0);
    m_count++;
  }

  public void test2() {
    Assert.assertEquals( m_count, 0);
    m_count++;
  }

  private static void ppp(String s) {
    System.out.println("[JUnitSample3] " + s);
  }
}
