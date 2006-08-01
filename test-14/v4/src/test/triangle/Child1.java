package test.triangle;



/**
 * This class
 * 
 * @author cbeust
 */
public class Child1 extends Base {
  /**
   * @testng.test
   */
  public void child1() {
    assert m_isInitialized : "Wasn't initialized correctly " + hashCode() + " " + getClass();

  }

  /**
   * @testng.test
   */
  public void child1a() {
    assert m_isInitialized : "Wasn't initialized correctly " + hashCode() + " " + getClass();
  }

}
