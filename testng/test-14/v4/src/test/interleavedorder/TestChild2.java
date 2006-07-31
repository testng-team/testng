package test.interleavedorder;


public class TestChild2 extends BaseTestClass {
  /**
   * @testng.configuration beforeTestClass="true"
   */
  public void beforeTestChildTwoClass() {
    InterleavedInvocationTest.LOG.append(getClass().getName() + ".beforeTestChildTwoClass");
  }
  
  /**
   * @testng.configuration afterTestClass="true"
   */
  public void afterTestChildTwoClass() {
    InterleavedInvocationTest.LOG.append(getClass().getName() + ".afterTestChildTwoClass");
  }
}
