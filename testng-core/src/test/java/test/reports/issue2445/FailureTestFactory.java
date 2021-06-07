package test.reports.issue2445;

import org.testng.annotations.Factory;

public class FailureTestFactory {
  @Factory
  public Object[] getTestClasses() {
    Object[] tests = new Object[2];
    tests[0] = new Test1();
    tests[1] = new Test2();
    return tests;
  }
}
