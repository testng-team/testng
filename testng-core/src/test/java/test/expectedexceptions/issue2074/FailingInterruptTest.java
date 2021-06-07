package test.expectedexceptions.issue2074;

import static org.testng.Assert.fail;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class FailingInterruptTest {

  @Test(expectedExceptions = RuntimeException.class, timeOut = 1000)
  public void shouldntInterruptTheThreadButDoesTest() {
    throw new RuntimeException("unit test exception");
  }

  @AfterMethod
  public void checkForInterrupts() {
    if (Thread.interrupted()) { // check for interrupts and clear the flag
      fail("Thread was interrupted but shouldn't have been");
    }
  }
}
