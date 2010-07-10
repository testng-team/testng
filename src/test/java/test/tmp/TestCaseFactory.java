package test.tmp;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class TestCaseFactory {
  class MyTestClass {
    @Test
    public void testAll() {
    }
  }

  @Factory
  public Object[] createTestCases() {
    Object[] testCases = new Object[1];
    testCases[0] = new MyTestClass() {
    };
    return testCases;
  }
}