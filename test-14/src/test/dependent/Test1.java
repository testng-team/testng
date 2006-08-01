package test.dependent;

public class Test1 {
  private boolean m_oneA = false;
  private boolean m_oneB = false;
  private boolean m_secondA = false;

  /**
   * @testng.test
   */
  public void oneA() {
//    ppp("oneA");
    assert ! m_secondA : "secondA shouldn't have been run yet";
    m_oneA = true;
  }
  
  /**
   * @testng.test dependsOnMethods = "oneA";
   */
  public void secondA() {
//    ppp("secondA");
    assert m_oneA : "oneA wasn't run";
    assert m_oneB : "oneB wasn't run";
    assert ! m_secondA : "secondA shouldn't have been run yet";
    m_secondA = true;
  }
}
