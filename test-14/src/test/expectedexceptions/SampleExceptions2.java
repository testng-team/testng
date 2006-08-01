package test.expectedexceptions;

/**
 * This class tests @ExpectedExceptions
 * 
 * @author cbeust
 */
public class SampleExceptions2 {

  /**
   * @testng.test expectedExceptions="java.lang.NumberFormatException"
   */
  public void shouldPass() {
    throw new java.lang.NumberFormatException();
  }

  /**
   * @testng.test expectedExceptions="java.lang.NumberFormatException"
   */
  public void shouldFail1() {
    throw new RuntimeException();
  }

  /**
   * @testng.test expectedExceptions="java.lang.NumberFormatException"
   */
  public void shouldFail2() {
  }
  
  
}
