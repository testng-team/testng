package test.expectedexceptions;


import org.testng.annotations.Test;

public class WrappedExpectedExceptionTest {
  @Test(timeOut = 1000L, expectedExceptions = { IllegalStateException.class })
  public void testTimeout() {
    throw new IllegalStateException("expected failure");
  }
}
