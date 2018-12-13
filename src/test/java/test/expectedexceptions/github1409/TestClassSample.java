package test.expectedexceptions.github1409;

import org.testng.annotations.Test;

public class TestClassSample {
  @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "expected")
  public void test() {
    throw new RuntimeException("actual");
  }
}
