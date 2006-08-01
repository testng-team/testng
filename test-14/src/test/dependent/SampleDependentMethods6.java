package test.dependent;

/**
 * This class
 * 
 * @author cbeust
 */
public class SampleDependentMethods6 {
  /**
   * @testng.test dependsOnMethods = "step2"
   */
  public void step1() {
  }

  /**
   * @testng.test dependsOnMethods = "step1"
   */
  public void step2() {
  }
}
