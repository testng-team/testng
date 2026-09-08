package org.testng.skip.samples;

import org.testng.annotations.BeforeTest;

public class TestClassWithOnlyGlobalConfig {
  @BeforeTest
  public void beforeTest() {
    throw new RuntimeException("simulating a failure");
  }
}
