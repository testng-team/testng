package test.skipex;

import org.testng.SkipException;
import org.testng.annotations.Test;

public class SkipAndExpectedSampleTest {
  @Test(expectedExceptions = NullPointerException.class)
  public void a2() {
    throw new SkipException("test");
  }

}
