package test.expectedexceptions;


/**
 * Regression test for bug with expected exceptions.
 * See forum http://forums.opensymphony.com/thread.jspa?threadID=4792&tstart=0
 */
public class WrappedUnwrappedExceptionTest {

  /**
   * @testng.test
   * @testng.expected-exceptions value="java.lang.RuntimeException"
   */
  public void runtimeWithNoCause() {
    throw new RuntimeException();
  }

  /**
   * @testng.test
   * @testng.expected-exceptions value="java.lang.RuntimeException"
   */
  public void runtimeWithCause() {
    try {
      throw new java.io.EOFException();
    }
    catch(java.io.IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
