package test.triangle;



/**
 * This class
 * 
 * @author cbeust
 */
public class Child2 extends Base {

  /**
   * @testng.test
   */
  public void child2() {
    assert m_isInitialized : "Wasn't initialized correctly";
  }
}
