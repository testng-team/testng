package test;

import org.testng.TestNG;

public class SimpleBaseTest {

  protected TestNG create() {
    TestNG result = new TestNG();
    result.setVerbose(0);
    return result;
  }
}
