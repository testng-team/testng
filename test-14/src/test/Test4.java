package test;


public class Test4 extends BaseTest {

  /**
   * @testng.test
   */
  public void expectedExceptions() {
    addClass("test.expectedexceptions.WrappedUnwrappedExceptionTest");
    run();
    String[] passed= { "runtimeWithNoCause", "runtimeWithCause" };
    verifyTests("Passed", passed, getPassedTests());
  }

}
