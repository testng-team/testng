package test.junit;

import junit.framework.TestCase;

/**
 * Test that the correct number of constructors is called
 *
 * <p>Created on Aug 9, 2005
 *
 * @author cbeust
 */
public class JUnitConstructorTest extends TestCase {
  private static int m_constructorCount = 0;
  private static int m_createCount = 0;
  private static int m_queryCount = 0;

  public JUnitConstructorTest(/*String string */ ) {
    m_constructorCount++;
  }

  public void testCreate() {
    m_createCount++;
  }

  public void testQuery() {
    m_queryCount++;
  }

  @Override
  public void tearDown() {
    assertEquals(3, m_constructorCount);
    assertTrue(
        (1 == m_createCount && 0 == m_queryCount) || (0 == m_createCount && 1 == m_queryCount));
  }
}
