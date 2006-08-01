package test.dependent;

/**
 * This class
 * 
 * @author cbeust
 */
public class SampleDependentMethods5 {
  
  /**
   * @testng.test
   */
  public void step1() {
  }

  /**
   * @testng.test dependsOnMethods = "step1 blablable"
   */
  public void step2() {
  }
}
