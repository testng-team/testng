package test.dependent;

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
    assert !m_secondA : "secondA shouldn't have been run yet";
    m_oneA = true;
  }

  @Parameters({"foo"})
  @Test
  public void one(String s) {
    assert !m_secondA : "secondA shouldn't have been run yet";
    assert "Cedric".equals(s) : "Expected parameter value Cedric but got " + s;
    m_oneB = true;
  }

  @Test(dependsOnMethods = {"one"})
  public void secondA() {
    assert m_oneA : "oneA wasn't run";
    assert m_oneB : "oneB wasn't run";
    assert !m_secondA : "secondA shouldn't have been run yet";
    m_secondA = true;
  }

  @AfterClass
  public void tearDown() {
    assert m_oneA : "oneA wasn't run";
    assert m_oneB : "oneB wasn't run";
    assert m_secondA : "secondA wasn't run";
  }
}
