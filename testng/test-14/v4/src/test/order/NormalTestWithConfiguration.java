package test.order;


public class NormalTestWithConfiguration {
  /**
   * @testng.configuration beforeTestClass="true"
   *
   */
  public void beforeTestClass() {
    System.out.println("NTWC.beforeTestClass");
    throw new RuntimeException("should stop everything at class level");
  }
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void beforeTestMethod() {
    System.out.println("NTWC.beforeTestMethod");
  }
  
  /**
   * @testng.test
   */
  public void test1() {
    System.out.println("NTWC.test1");
  }
  
  /**
   * @testng.test
   */
  public void test2() {
    System.out.println("NTWC.test2");
  }
  
  /**
   * @testng.configuration afterTestMethod="true"
   *
   */
  public void afterTestMethod() {
    System.out.println("NTWC.afterTestMethod");
  }
  
  /**
   * @testng.configuration afterTestClass="true"
   */
  public void afterTestClass() {
    System.out.println("NTWC.afterTestClass");
  }
}
