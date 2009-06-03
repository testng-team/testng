package test;

import org.testng.TestNG;

public class SimpleBaseTest {

  protected TestNG create() {
    TestNG result = new TestNG();
    result.setVerbose(0);
    return result;
  }
  
  protected TestNG create(Class testClass) {
    TestNG result = create();
    result.setTestClasses(new Class[] { testClass});
    return result;
  }

  protected TestNG create(Class[] testClasses) {
    TestNG result = create();
    result.setTestClasses(testClasses);
    return result;
  }
}
