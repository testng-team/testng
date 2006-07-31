package test.expectedexceptions;


import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

public class WrappedExpectedException {
  @Test(timeOut = 1000L)
  @ExpectedExceptions({ IllegalStateException.class })
  public void testTimeout() {
    throw new IllegalStateException("expected failure");
  }
}
