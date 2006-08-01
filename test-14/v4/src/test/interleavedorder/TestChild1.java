package test.interleavedorder;


public class TestChild1 extends BaseTestClass {
  /**
   * @testng.configuration beforeTestClass="true"
   */
  public void beforeTestChildOneClass() {
    InterleavedInvocationTest.LOG.append(getClass().getName() + ".beforeTestChildOneClass");
  }
  
  /**
   * @testng.configuration afterTestClass="true"
   */
  public void afterTestChildOneClass() {
    InterleavedInvocationTest.LOG.append(getClass().getName() + ".afterTestChildOneClass");
  }
}
