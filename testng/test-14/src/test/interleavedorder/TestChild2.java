package test.interleavedorder;


public class TestChild2 extends BaseTestClass {
  /**
   * @testng.before-class
   */
  public void beforeTestChildTwoClass() {
    InterleavedInvocationTest.LOG.append(getClass().getName() + ".beforeTestChildTwoClass");
  }
  
  /**
   * @testng.after-class
   */
  public void afterTestChildTwoClass() {
    InterleavedInvocationTest.LOG.append(getClass().getName() + ".afterTestChildTwoClass");
  }
}
