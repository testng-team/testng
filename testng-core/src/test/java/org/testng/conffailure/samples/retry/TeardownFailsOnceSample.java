package org.testng.conffailure.samples.retry;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * The first attempt fails and its teardown fails too. The retry must get its setup and its teardown
 * back: what the first attempt recorded is not held against the attempt that retries it.
 */
public class TeardownFailsOnceSample {

  private int attempt;
  private boolean setUp;

  @BeforeMethod
  public void setup() {
    setUp = true;
  }

  @Test(retryAnalyzer = RetryOnce.class)
  public void flaky() {
    attempt++;
    assertThat(setUp).withFailMessage("attempt " + attempt + " ran without setup").isTrue();
    setUp = false;
    if (attempt == 1) {
      throw new AssertionError("first attempt fails");
    }
  }

  @AfterMethod
  public void teardown() {
    if (attempt == 1) {
      throw new IllegalStateException("teardown fails after the first attempt");
    }
  }
}
