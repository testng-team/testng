package test.sample;



/**
 * This class
 *
 * @author Cedric Beust, Apr 26, 2004
 */
public class Sample1 extends BaseSample1 {
  /**
   * @testng.configuration afterTestClass="true"
   */
  public static void tearDownClass1() {
  }

  /**
   * @testng.configuration afterTestClass="true"
   */
  public void tearDownClass2() {
  }

  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void beforeTest() {
  }

  /**
   * @testng.configuration afterTestMethod="true"
   */
  public void afterTest() {
  }

  /**
   * @testng.test groups="even"
   */
  public void method2() {
  }
  
  // Method moved to base class to test inheritance
//  @Test(groups = { "odd" })
//  public void method1() {
//  }

  /**
   * @testng.test groups="odd"
   */
  public void method3() {
  }

  /**
   * @testng.test enabled="false" groups="odd"
   */
  public void oddDisableMethod() {
  }

  /**
   * @testng.test groups="broken"
   */
  public void broken() {
  }

  /**
   * @testng.test groups="fail"
   * @testng.expected-exceptions value="java.lang.NumberFormatException,java.lang.ArithmeticException"
   */
  public void throwExpectedException1ShouldPass() {
    throw new NumberFormatException();
  }

  /**
   * @testng.test groups="fail"
   * @testng.expected-exceptions value="java.lang.NumberFormatException,java.lang.ArithmeticException"
   */
  public void throwExpectedException2ShouldPass() {
    throw new ArithmeticException();
  }

  /**
   * @testng.test groups="fail ,  bug "
   */
  public void throwExceptionShouldFail() {
    throw new NumberFormatException();
  }

  /**
   * @testng.test groups="assert"
   */
  public void verifyLastNameShouldFail() {
    assert "Beust".equals("") : "Expected name Beust, found blah";
  }

  private static void ppp(String s) {
    System.out.println("[Test1] " + s);
  }


}
