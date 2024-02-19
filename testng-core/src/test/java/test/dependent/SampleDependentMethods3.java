package test.dependent;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This class tests overloaded dependent methods
 *
 * @author Cedric Beust, Aug 19, 2004
 */
public class SampleDependentMethods3 {
  private boolean m_oneA = false;
  private boolean m_oneB = false;
  private boolean m_secondA = false;

  @Test
  public void one() {
    assertFalse(m_secondA, "secondA shouldn't have been run yet");
    m_oneA = true;
  }

  @Parameters({"foo"})
  @Test
  public void one(String s) {
    assertFalse(m_secondA, "secondA shouldn't have been run yet");
    assertEquals(s, "Cedric", "Expected parameter value Cedric but got " + s);
    m_oneB = true;
  }

  @Test(dependsOnMethods = {"one"})
  public void secondA() {
    assertTrue(m_oneA, "oneA wasn't run");
    assertTrue(m_oneB, "oneB wasn't run");
    assertFalse(m_secondA, "secondA shouldn't have been run yet");
    m_secondA = true;
  }

  @AfterClass
  public void tearDown() {
    assertTrue(m_oneA, "oneA wasn't run");
    assertTrue(m_oneB, "oneB wasn't run");
    assertTrue(m_secondA, "secondA wasn't run");
  }
}
