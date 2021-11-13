package test.dependent;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * This class exercises dependent methods
 *
 * @author Cedric Beust, Aug 19, 2004
 */
public class SampleDependentMethods2 {
  private boolean m_oneA = false;
  private boolean m_oneB = false;
  private boolean m_secondA = false;
  private boolean m_thirdA = false;

  @Test(groups = {"one"})
  public void oneA() {
    assert !m_secondA : "secondA shouldn't have been run yet";
    m_oneA = true;
  }

  @Test
  public void canBeRunAnytime() {}

  @Test(dependsOnGroups = {"one"})
  public void secondA() {
    assert m_oneA : "oneA wasn't run";
    assert m_oneB : "oneB wasn't run";
    assert !m_secondA : "secondA shouldn't have been run yet";
    m_secondA = true;
  }

  @Test(dependsOnMethods = {"secondA"})
  public void thirdA() {
    assert m_oneA : "oneA wasn't run";
    assert m_oneB : "oneB wasn't run";
    assert m_secondA : "secondA wasn't run";
    assert !m_thirdA : "thirdA shouldn't have been run yet";
    m_thirdA = true;
  }

  @Test(groups = {"one"})
  public void oneB() {
    assert !m_secondA : "secondA shouldn't have been run yet";
    m_oneB = true;
  }

  @AfterClass
  public void tearDown() {
    assert m_oneA : "oneA wasn't run";
    assert m_oneB : "oneB wasn't run";
    assert m_secondA : "secondA wasn't run";
    assert m_thirdA : "thirdA wasn't run";
  }
}
