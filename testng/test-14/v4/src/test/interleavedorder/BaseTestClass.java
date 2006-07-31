package test.interleavedorder;


public class BaseTestClass {
  /**
   * @testng.test
   */
  public void testOne() {
    InterleavedInvocationTest.LOG.append(getClass().getName() + ".testOne");
  }
  
  /**
   * @testng.test
   */
  public void testTwo() {
    InterleavedInvocationTest.LOG.append(getClass().getName() + ".testTwo");
  }
  
  /**
   * @testng.test
   */
  public void testThree() {
    InterleavedInvocationTest.LOG.append(getClass().getName() + ".testThree");
  }
}
