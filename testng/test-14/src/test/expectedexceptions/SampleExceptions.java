package test.expectedexceptions;

/**
 * This class tests @ExpectedExceptions
 * 
 * @author cbeust
 */
public class SampleExceptions {

  /**
   * @testng.test
   * @testng.expected-exceptions value="java.lang.NumberFormatException"
   */
  public void shouldPass() {
    throw new java.lang.NumberFormatException();
  }

  /**
   * @testng.test
   * @testng.expected-exceptions value="java.lang.NumberFormatException"
   */
  public void shouldFail1() {
    throw new RuntimeException();
  }

  /**
   * @testng.test
   * @testng.expected-exceptions value="java.lang.NumberFormatException"
   */
  public void shouldFail2() {
  }
  
  
}
