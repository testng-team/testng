package org.testng.internal.issue1456;

import org.testng.ITest;

public class TestClassSample implements ITest {
  private String testname;

  public TestClassSample(String testname) {
    this.testname = testname;
  }

  @Override
  public String getTestName() {
    return testname;
  }
}
