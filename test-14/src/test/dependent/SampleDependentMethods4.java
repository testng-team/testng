package test.dependent;

/**
 * This class
 * 
 * @author cbeust
 */
public class SampleDependentMethods4 {

  /**
   * @testng.test
   */
  public void step1() {
  } 

  /**
   * @testng.test dependsOnMethods = "step1"
   */
  public void step2() {
    assert false : "Problem in step2";
  }

  /**
   * @testng.test dependsOnMethods = "step2"
   */
  public void step3() {
  }
}
