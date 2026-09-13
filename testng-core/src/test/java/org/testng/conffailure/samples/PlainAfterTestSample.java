package org.testng.conffailure.samples;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * A test-level pair with no {@code alwaysRun}. A test-level configuration runs with no test
 * instance, so the failure check must answer for a null instance.
 */
public class PlainAfterTestSample {

  @BeforeTest
  public void setup() {
    throw new RuntimeException("Fail the test.");
  }

  @Test
  public void execute() {}

  @AfterTest
  public void cleanup() {}
}
