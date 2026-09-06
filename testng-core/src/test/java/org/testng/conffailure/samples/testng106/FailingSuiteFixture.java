package org.testng.conffailure.samples.testng106;

import org.testng.annotations.BeforeSuite;

/** TESTNG-106: failing @BeforeSuite doesn't skip all tests */
public class FailingSuiteFixture {
  public static int s_invocations = 0;

  @BeforeSuite
  public void failingBeforeSuite() {
    throw new RuntimeException();
  }
}
