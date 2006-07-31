
package test.sample;



/**
 * This class verifies the PartialGroupTest
 * 
 * @author cbeust
 */

public class PartialGroupVerification {
  /**
   * @testng.test
   */
  public void verify() {
    assert PartialGroupTest.m_successMethod && PartialGroupTest.m_successClass
      : "test1 and test2 should have been invoked both";
  }
}