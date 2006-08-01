package test.dependent;

/**
 * This class tests overloaded dependent methods
 *
 * @author Cedric Beust, Aug 19, 2004
 * 
 */
public class SampleDependentMethods3 {
  private boolean m_oneA = false;
  private boolean m_oneB = false;
  private boolean m_secondA = false;
  
  /**
   * @testng.test
   */
  public void one() {
//    ppp("oneA");
    assert ! m_secondA : "secondA shouldn't have been run yet";
    m_oneA = true;
  }
  
  /**
   * @testng.parameters value = "foo"
   * @testng.test
   */
  public void one(String s) {
//    ppp("oneB");
    assert ! m_secondA : "secondA shouldn't have been run yet";
    assert "Cedric".equals(s) : "Expected parameter value Cedric but got " + s;
    m_oneB = true;    
  }

  /**
   * @testng.test dependsOnMethods = "one"
   */
  public void secondA() {
//    ppp("secondA");
    assert m_oneA : "SampleDependentMethods3.oneA wasn't run";
    assert m_oneB : "SampleDependentMethods3.oneB wasn't run";
    assert ! m_secondA : "secondA shouldn't have been run yet";
    m_secondA = true;
  }

  /**
   * @testng.after-class
   */
  public void tearDown() {
    assert m_oneA : "SampleDependentMethods3.oneA wasn't run";
    assert m_oneB : "SampleDependentMethods3.oneB wasn't run";
    assert m_secondA : "secondA wasn't run";
  }
  
  public static void ppp(String s) {
    System.out.println("[SampleDependentMethods] " + s);
  }
}