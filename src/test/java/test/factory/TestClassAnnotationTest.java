package test.factory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * Make sure that @Factory methods are not counted as @Test in the
 * presence of a class-scoped @Test annotation.
 *
 * Created on Mar 30, 2006
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
@Test
public class TestClassAnnotationTest {

  private int m_count;

  @Factory
  public Object[] createFixture() {
    ppp("FACTORY");
    m_count++;
    return new Object[] { new Object[] { new Object() }};
  }

  public void testOne() {
    ppp("TESTONE");
    m_count++;
  }

  @AfterClass
  public void verify() {
    ppp("VERIFY");
    Assert.assertEquals(m_count, 2);
  }

  private static void ppp(String s) {
    if (false) {
      System.err.println("[FactoryTest] " + s);
    }
  }

}
