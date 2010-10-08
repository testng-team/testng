package test.dependent;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;


/**
 * This class exercises dependent methods
 *
 * @author Cedric Beust, Aug 19, 2004
 *
 */
public class SampleDependentMethods {
  private boolean m_oneA = false;
  private boolean m_oneB = false;
  private boolean m_secondA = false;
  private boolean m_thirdA = false;

  @Test
  public void oneA() {
//    ppp("oneA");
//    assert false : "TEMPORARY FAILURE";
    assert ! m_secondA : "secondA shouldn't have been run yet";
    m_oneA = true;
  }

  @Test
  public void canBeRunAnytime() {

  }

  @Test(dependsOnMethods= { "oneA", "oneB" })
  public void secondA() {
//    ppp("secondA");
    assert m_oneA : "oneA wasn't run";
    assert m_oneB : "oneB wasn't run";
    assert ! m_secondA : "secondA shouldn't have been run yet";
    m_secondA = true;

  }

  @Test(dependsOnMethods= { "secondA" })
  public void thirdA() {
//    ppp("thirdA");
    assert m_oneA : "oneA wasn't run";
    assert m_oneB : "oneB wasn't run";
    assert m_secondA : "secondA wasn't run";
    assert ! m_thirdA : "thirdA shouldn't have been run yet";
    m_thirdA = true;

}

  @Test
  public void oneB() {
//    ppp("oneB");
    assert ! m_secondA : "secondA shouldn't have been run yet";
    m_oneB = true;
  }

  @AfterClass
  public void tearDown() {
    assert m_oneA : "oneA wasn't run";
    assert m_oneB : "oneB wasn't run";
    assert m_secondA : "secondA wasn't run";
    assert m_thirdA : "thirdA wasn't run";
  }

  public static void ppp(String s) {
    System.out.println("[SampleDependentMethods] " + s);
  }
}