package test.expectedexceptions.issue2235;

import org.testng.annotations.Test;

public class ExampleTestCase {

  @Test(timeOut = 1000, expectedExceptions = IllegalArgumentException.class)
  public void testMethod() {
    throw new IllegalArgumentException("foo");
  }
}
