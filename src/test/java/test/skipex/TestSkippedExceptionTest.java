package test.skipex;

import org.testng.SkipException;
import org.testng.TimeBombSkipException;
import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class TestSkippedExceptionTest {
  @Test
  public void genericSkipException() {
    throw new SkipException("genericSkipException is skipped for now");
  }

  @Test
  public void timedSkipException() {
    throw new TimeBombSkipException("timedSkipException is time bombed", "2007/04/10");
  }
}
